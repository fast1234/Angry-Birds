
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.VisionMBR;

public class NaiveAgent implements Runnable {

      public int indi=0;
    public List<storage> Table = new ArrayList<storage>();
	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel =1 ;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
   public learning l=new learning();
	// a standalone implementation of the Naive Agent
	public NaiveAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);


        System.out.println("called run");
		while (true) {
			GameState state = solve();

             // indi++;
			if (state == GameState.WON) {
				try {

					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}

                int i=0;
                int size=Table.size();
                while(i<size){
                    Table.get(i).state=1;
                    Table.get(i).score=scores.get(currentLevel);
                    l.getvalue(Table.get(i));
                   // Table.remove(i);
                          i++;

                }
                Table.clear();

                indi=0;
				System.out.println("Total Score: " + totalScore);
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
                int i=0;
                int size=Table.size();
                while(i<size){
                    Table.get(i).state=0;
                    Table.get(i).score=0;

                    l.getvalue(Table.get(i));
                    // Table.remove(i);
                    i++;

                }
                Table.clear();

                indi=0;

                System.out.println("Restart");
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}


    public GameState solve()
    {
        storage R=new storage();

        // capture Image
        BufferedImage screenshot = ActionRobot.doScreenShot();


        R.addImage(screenshot);


        // process image
        VisionMBR vision = new VisionMBR(screenshot);
        Vision vi =new Vision(screenshot);
        List<ABObject> hills=vi.findHills();
        // find the slingshot
        Rectangle sling = vision.findSlingshotMBR();

        // confirm the slingshot
        while (sling == null && aRobot.getState() == GameState.PLAYING) {
            System.out
                    .println("No slingshot detected. Please remove pop up or zoom out");
            ActionRobot.fullyZoomOut();
            screenshot = ActionRobot.doScreenShot();
            vision = new VisionMBR(screenshot);
            sling = vision.findSlingshotMBR();
        }

        List<Rectangle> red_birds = vision.findRedBirdsMBRs();
        List<Rectangle> blue_birds = vision.findBlueBirdsMBRs();
        List<Rectangle> yellow_birds = vision.findYellowBirdsMBRs();
        List<Rectangle> stone = vision.findStonesMBR();
        List<Rectangle> ice = vision.findIceMBR();
        List<Rectangle> wood = vision.findWoodMBR();
        List<Rectangle> pigs = vision.findPigsMBR();

        Calulation weakSpotCalulation = new Calulation();
        weakSpotCalulation.addWoodenObstacles(wood);
        weakSpotCalulation.addIceObstacles(ice);
        weakSpotCalulation.addStoneObstacles(stone);
        weakSpotCalulation.addPigs(pigs);

        int bird_count = 0;
        bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();


        // get all the pigs
        //List<ABObject> pigs = vision.findPigsMBR();

        GameState state = aRobot.getState();

        // if there is a sling, then play, otherwise just skip.
        if (sling != null) {

            if (!pigs.isEmpty()) {

                Point releasePoint = null;
                Shot shot = new Shot();
                int dx,dy;
                {
                    // random pick up a pig
                    //	ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));

                    //	Point _tpt = pig.getCenter();// if the target is very close to before, randomly choose a
                    // point near it
                    Random r = new Random();
                    int index = r.nextInt(pigs.size());
                    Rectangle pig = pigs.get(index);
                    Point _tpt = new Point((int) pig.getCenterX(),
                            (int) pig.getCenterY());

                    List<Point> weakSpots = weakSpotCalulation.gettarget();
                    _tpt = weakSpots.get(0); //always contains at least one point

                    if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
                        double _angle = randomGenerator.nextDouble() * Math.PI * 2;
                        _tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
                        _tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
                        System.out.println("Randomly changing to " + _tpt);
                    }

                    prevTarget = new Point(_tpt.x, _tpt.y);
                    // estimate the trajectory
                    ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

                    // do a high shot when entering a level to find an accurate velocity

                 //   ABObject h=hills.get(0);
                    int ss=0;
                    int max=hills.size();
                    for(int i=0;i<max;i++){
                       ABObject  h=hills.get(i);
                        if(h.getX()<_tpt.x){
                            if(h.getY()<_tpt.y)
                            { ss=1;
                            break;}
                        }


                    }

                     if (pts.size() == 1){
                        // System.out.println("only one point");
                        releasePoint = pts.get(0);}
                    else if (pts.size() == 2)

                    {
                             if(ss==1){
                                releasePoint=pts.get(1);
                             }
                          //       System.out.println("ssis 1");}
                        // randomly choose between the trajectories, with a 1 in
                        // 6 chance of choosing the high one
                     // if (randomGenerator.nextInt(6) == 0)
                           // releasePoint = pts.get(1);
                       else
                            releasePoint = pts.get(0);
                    }
                    else
                    if(pts.isEmpty())
                    {
                        System.out.println("No release point found for the target");
                        System.out.println("Try a shot with 45 degree");
                        releasePoint = tp.findReleasePoint(sling, Math.PI/4);
                    }
                    // R.addAngle(releasePoint);

                    // Get the reference point
                    Point refPoint = tp.getReferencePoint(sling);

                    double releaseAngle = tp.getReleaseAngle(sling,
                            releasePoint);

                    //Calculate the tapping time according the bird type
                    if (releasePoint != null) {

                        System.out.println("Release Point: " + releasePoint);
                        System.out.println("Release Angle: "
                                + Math.toDegrees(releaseAngle));
                        if(l.calvalue(screenshot)>0)
                        {
                            System.out.println("I am learning");
                            releaseAngle=l.calvalue(screenshot);
                        }
                        else
                        {
                            if(releaseAngle==l.calvalue(screenshot)){
                                if (randomGenerator.nextInt(6) == 0)
                                    releasePoint = pts.get(1);
                                else
                                    releasePoint = pts.get(0);
                                releaseAngle = tp.getReleaseAngle(sling,
                                        releasePoint);



                            }
                        }
                        int tapInterval = 0;
                        R.addAngle(releaseAngle);

                        switch (aRobot.getBirdTypeOnSling())
                        {

                            case RedBird:
                                tapInterval = 0; break;               // start of trajectory
                            case YellowBird:
                                tapInterval = 75 + randomGenerator.nextInt(10);break; // 65-90% of the way
                            case WhiteBird:
                                tapInterval =  75 + randomGenerator.nextInt(10);break; // 70-90% of the way
                            case BlackBird:
                                tapInterval = 70 + randomGenerator.nextInt(10);break; // 70-90% of the way
                            case BlueBird:
                                tapInterval =  70 + randomGenerator.nextInt(15);break; // 65-85% of the way
                            default:
                                tapInterval =  80;
                        }
                        int tap_time = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
                        dx = (int)releasePoint.getX() - refPoint.x;
                        dy = (int)releasePoint.getY() - refPoint.y;
                        shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tap_time);
                    }
                    else
                    {
                        System.err.println("No Release Point Found");
                        return state;
                    }
                }

                // check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
                {
                    ActionRobot.fullyZoomOut();
                    screenshot = ActionRobot.doScreenShot();
                    vision = new VisionMBR(screenshot);
                    Rectangle _sling = vision.findSlingshotMBR();
                    if(_sling != null)
                    {
                        double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
                        if(scale_diff < 25)
                        {
                            if(dx < 0)
                            {
                                aRobot.cshoot(shot);
                                state = aRobot.getState();
                                if ( state == GameState.PLAYING )
                                {
                                    screenshot = ActionRobot.doScreenShot();
                                    vision = new VisionMBR(screenshot);
                                    List<Point> traj = vision.findTrajPoints();
                                    tp.adjustTrajectory(traj, sling, releasePoint);
                                    firstShot = false;
                                }
                            }
                        }
                        else
                            System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
                    }
                    else
                        System.out.println("no sling detected, can not execute the shot, will re-segement the image");
                }

            }

        }
        //System.out.println(R.angle+"Angle in naive");
        if(R.angle!=0 && R.screenshot!=null){
                Table.add(indi,R);
            indi++;
           // System.out.println(Table.get(indi).angle+"angle which we added");
        }
        return state;
    }


    public static void main(String args[]) {

		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
