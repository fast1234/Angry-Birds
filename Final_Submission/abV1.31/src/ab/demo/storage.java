package ab.demo;

import ab.vision.ABObject;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class storage{


    double angle;
    public BufferedImage screenshot;
    double score;
    int state;


    public void addScore(int _score){
        score = _score;
    }

    public void addFlag(int state){
        this.state = state;
    }

    public void addImage(BufferedImage _screenshot){
        screenshot = _screenshot;
    }

    public void addAngle(double angle){
        this.angle=angle;
    }

    public void add(storage R){
        this.angle=angle;

        this.screenshot = R.screenshot;
      this.state=R.state;
    }
}