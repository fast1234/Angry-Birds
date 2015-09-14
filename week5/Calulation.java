package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;

public class Calulation {
    private List<Rectangle> woodObstacles;
    private List<Rectangle> iceObstacles;
    private List<Rectangle> stoneObstacles;
    private List<Rectangle> pigs;

    public Calulation() {
        System.out.println("Object is created");
    }

    public void addWoodenObstacles(List<Rectangle> obstacles) {
        this.woodObstacles = obstacles;
    }

    public void addIceObstacles(List<Rectangle> obstacles) {
        this.iceObstacles = obstacles;
    }

    public void addStoneObstacles(List<Rectangle> obstacles) {
        this.stoneObstacles = obstacles;
    }

    public void addPigs(List<Rectangle> pigs) {
        this.pigs = pigs;
    }

    private LinkedList<Rectangle> getJointObstacles() {
        LinkedList<Rectangle> obstacles = new LinkedList<Rectangle>();
        obstacles.addAll(this.woodObstacles);
        obstacles.addAll(this.iceObstacles);
        obstacles.addAll(this.stoneObstacles);

        return obstacles;
    }

    private Rectangle getSegmentBorder(LinkedList<Rectangle> segment) {
        if(segment == null || segment.size() < 1) return null;

        double top = Double.MAX_VALUE;
        double bottom = Double.MIN_VALUE;
        double left = Double.MAX_VALUE;
        double right = Double.MIN_VALUE;
        for(Rectangle i : segment) {
            if(i.getY() < top) top = i.getY();
            if(i.getY()    + i.getHeight() > bottom) bottom = i.getY() + i.getHeight();
            if(i.getX()    + i.getWidth() > right) right = i.getX() + i.getWidth();
            if(i.getX()    < left) left = i.getX();
        }
        System.out.println("contains "+segment.size()+" rects. bounds are left:"+(int)left+" right:"+(int)right+" top:"+(int)top+" bottom:"+(int)bottom);
        return new Rectangle((int) left, (int) top, (int) (right-left), (int) (bottom-top));
    }

    private int countPigsInSegment(LinkedList<Rectangle> segment) {
        Rectangle segmentBorder = this.getSegmentBorder(segment);
        int pigCount = 0;
        for(Rectangle i : this.pigs) {
            if(segmentBorder.contains(i)) {
                pigCount++;
            }
        }

        return pigCount;
    }

    private LinkedList<LinkedList<Rectangle>> getSegment() {
        int TRESHOLD = 4;

        if(this.woodObstacles == null && this.iceObstacles == null &&
                this.stoneObstacles == null){
            return null;
        }
        else {
            LinkedList<Rectangle> obstacles = this.getJointObstacles();
            LinkedList<LinkedList<Rectangle>> segmentlist = new LinkedList<LinkedList<Rectangle>>();
            segmentlist.add(new LinkedList<Rectangle>());
            segmentlist.getFirst().add(obstacles.poll());

            boolean extendedSegment;
            while(obstacles.size() > 0) { //as long as not all obstacles are in some segment, segment them
                extendedSegment = false; //indicates if one of the segments were extended
                for(int i=0; i<segmentlist.size(); ++i) { //iterate over all currently existing segments
                    for(int j=0; j<segmentlist.get(i).size(); ++j) { //search if there is an obstacle that belongs to this segment
                        Rectangle dummy = new Rectangle(segmentlist.get(i).get(j));
                        dummy.grow(TRESHOLD, TRESHOLD);
                        for(int k=0; k<obstacles.size(); ++k) { //iterate over remaining obstacles
                            if(obstacles.get(k).intersects(dummy)) { //if there is an obstacle belonging to this segment
                                segmentlist.get(i).add(obstacles.get(k)); //add the obstacle
                                obstacles.remove(k);

                                //start searching again
                                j = segmentlist.get(i).size()-1;
                                i = segmentlist.size()-1;
                                extendedSegment = true;
                                break;
                            }
                        }
                    }
                }

                //if none of the remaining obstacles belong to an existing segment
                if(!extendedSegment) {
                    //create a new segment
                    LinkedList temp = new LinkedList<Rectangle>();
                    temp.add(obstacles.poll());
                    segmentlist.add(temp);
                }
            }
            //THORSTENS CODE END

            return segmentlist;
        }
    }



    //should return a list of the leftmost (attackable) items in a given segment
//suggested to use in weakPoints... after choosing a segment
    public LinkedList<Rectangle> targetsInSegment(LinkedList<Rectangle> segment){

        LinkedList<Rectangle> targets = new LinkedList<Rectangle>();
        targets.add(segment.poll());
        while(segment.size()>0){
            for(int i=0; i<targets.size(); i++) {
                Rectangle a = new Rectangle(targets.get(i));
                Rectangle b = new Rectangle(segment.getFirst());
                if((a.getY()+a.getHeight() < b.getY()) || (a.getY() > b.getY()+b.getHeight())){
                    targets.add(segment.poll());
                }
                else {
                    if(a.getX() < b.getX()){
                        segment.pop();
                    }
                    else {
                        targets.add(segment.poll());
                        targets.pop();
                    }
                }
            }
        }
        return targets;

    }


    public List<Point> weakSpots() {
        System.out.println("Searching weak spots for  birds");

        //find segments
        LinkedList<LinkedList<Rectangle>> segments = this.getSegment();//getSegments();
        System.out.println("Found "+segments.size()+" segment(-s)");

        //search the segment containing the most pigs
        int pigs=0, index=0;
        for(int i=0; i<segments.size(); ++i) {
            System.out.println("SEGMENT #"+i+":");
            int numberOfPigs = this.countPigsInSegment(segments.get(i));
            System.out.println("- segment contains "+numberOfPigs+" pigs\n");
            if(numberOfPigs > pigs) {
                pigs = numberOfPigs;
                index = i;
            }
        }

        //if a segment contains pigs, attack segment, otherwise attack first pig
        LinkedList<Point> weakSpots = new LinkedList<Point>();
        if(pigs > 0) {
            System.out.println("Segment #"+index+" contains most of the pigs, attack it!");
            Rectangle promisingSegmentsBoundingBox = this.getSegmentBorder(segments.get(index));
            Point center = new Point((int) (promisingSegmentsBoundingBox.getX() +
                    (promisingSegmentsBoundingBox.getWidth()/2)),
                    (int) (promisingSegmentsBoundingBox.getY() +
                            (promisingSegmentsBoundingBox.getHeight()/2)));
            weakSpots.add(center);
            System.out.println("using weak structure as target point " + center);
        } else if(pigs == 0 && this.pigs.size() > 0) {
            System.out.println("No segment contains pigs, attack pig directly!");
            Rectangle firstPig = this.pigs.get(0);
            Point target = new Point((int) firstPig.getX(), (int) firstPig.getY());
            weakSpots.add(target);
            System.out.println("using pig as target point "+target);
        }
        return weakSpots;
    }

}
