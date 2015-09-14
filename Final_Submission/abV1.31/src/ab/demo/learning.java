package ab.demo;

import ab.vision.Vision;
import ab.vision.ABObject;
import ab.vision.VisionUtils;
import ab.planner.TrajectoryPlanner;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;

public class learning {


    public static List<storage> Table = new ArrayList<storage>();
    private int count;


    private int threshold=25000;

    private int good;
    public learning(){

    }

    public double calvalue(BufferedImage image){
        if(Table != null && image != null) {


            for (storage prev : Table) {
                int dI = Math.abs(VisionUtils.numPixelsDifferent(prev.screenshot, image));
                if (dI <= threshold) {
                    if (prev.state == 1) {
                        System.out.println(prev.angle + "    Angle ");
                        return prev.angle;

                    } else {
                        double ang = -1 * (prev.angle);
                        return ang;

                    }


                }
                return 0;
            }

        }
        return 0;
    }



    public void getvalue(BufferedImage image, double _angle, Vision vision,int state){
        storage R = new storage();
        R.addImage(image);
        R.addAngle(_angle);
        R.addFlag(state);
        Table.add(R);
        count++;

    }

    public void getvalue(storage _storage){
        Table.add(_storage);
        count ++;
    }

}