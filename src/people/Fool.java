package people;

import rocket.room.*;
import space.objects.SpaceObject;

import java.io.Serializable;
import java.time.ZonedDateTime;

import static timeline.Timeline.*;

public class Fool extends Donut{
    private final Thumb thumb;

    class Thumb implements Serializable {
        private final int length;
        Thumb(int length){
            this.length = length;
        }
        int getLength(){
            return length;
        }
        public boolean equals(Thumb thumb){
            boolean equals = false;
            if (length == thumb.getLength()){
                equals = true;
            }
            return equals;
        }
        public String toString(){
            return "палец";
        }
    }

    public Fool(String name, int timeUntilHunger,  Room room, int thumbLength, String username){
        super(name, timeUntilHunger, room, username);
        thumb = new Thumb(thumbLength);
    }
    public Fool(String name, int timeUntilHunger, Room room, String foodName, int thumbLength, String username){
        super(name, timeUntilHunger, room, foodName, username);
        thumb = new Thumb(thumbLength);
    }
    public Fool(String name, int timeUntilHunger, Room room, int thumbLength, ZonedDateTime time, String username){
        super(name, timeUntilHunger, room, time, username);
        thumb = new Thumb(thumbLength);
    }
    public Fool(String name, int timeUntilHunger, Room room, String foodName, int thumbLength, ZonedDateTime time, String username){
        super(name, timeUntilHunger, room, foodName, time, username);
        thumb = new Thumb(thumbLength);
    }
    public void seems(int distance, int rocketSpeed){
        String str = getCurrentTime() + " часов: " + name + ": кажется, что ";
        if(distance/rocketSpeed > thumb.getLength()/2){
            str += "ракета стоит на месте";
        }else{
            str += "ракета летит";
        }
        System.out.println(str + ".");
    }

    public void lookAt(SpaceObject spaceObject){
        System.out.println(getCurrentTime() + " часов: " + name + ": смотрит на " + spaceObject.toString());
    }

    public int getThumbLength() {
        return thumb.getLength();
    }

    public boolean equals(Fool human) {
        return super.equals(human)
                &&getThumbLength() == human.getThumbLength()
                &&foodName.equals(human.getFoodName());
    }
}