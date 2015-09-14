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
        //System.out.println("contains "+segment.size()+" rects. bounds are left:"+(int)left+" right:"+(int)right+" top:"+(int)top+" bottom:"+(int)bottom);
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
            while(obstacles.size() > 0) {
                extendedSegment = false;
                for(int i=0; i<segmentlist.size(); ++i) {
                    for(int j=0; j<segmentlist.get(i).size(); ++j) {
                        Rectangle dummy = new Rectangle(segmentlist.get(i).get(j));
                        dummy.grow(3,3);
                        for(int k=0; k<obstacles.size(); ++k) {
                            if(obstacles.get(k).intersects(dummy)) {
                                segmentlist.get(i).add(obstacles.get(k));
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


                if(!extendedSegment) {
                    LinkedList temp = new LinkedList<Rectangle>();
                    temp.add(obstacles.poll());
                    segmentlist.add(temp);
                }
            }


            return segmentlist;
        }
    }



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


    public List<Point> gettarget() {
        System.out.println("i am calulating target spot");

        //find segments
        LinkedList<LinkedList<Rectangle>> segments = this.getSegment();


        //search the segment containing the most pigs

        int pigs=0, index=0;
        for(int i=0; i<segments.size(); ++i) {
            int numberOfPigs = this.countPigsInSegment(segments.get(i));
            if(numberOfPigs > pigs) {
                pigs = numberOfPigs;
                index = i;
            }
        }


        LinkedList<Point> gettarget = new LinkedList<Point>();
        if(pigs > 0) {
            Rectangle tar = this.getSegmentBorder(segments.get(index));
            Point center = new Point((int) (tar.getX() +
                    (tar.getWidth()/2)),
                    (int) (tar.getY() +
                            (tar.getHeight()/2)));
            gettarget.add(center);
           

        } else if(pigs == 0 && this.pigs.size() > 0) {

            Rectangle firstPig = this.pigs.get(0);
            Point target = new Point((int) firstPig.getX(), (int) firstPig.getY());
            gettarget.add(target);
        }
        return gettarget;
    }

}
