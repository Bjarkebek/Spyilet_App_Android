package bb.tec.spyiletapp.Model;

import android.location.Location;

import bb.tec.spyiletapp.MainActivity;

public class ToiletLocation {
    private double latitude;
    private double longitude;
    private double altitude;
    private float direction;


    public void registerToiletLocation(Location location){
          latitude = location.getLatitude();
          longitude = location.getLongitude();
          altitude = location.getAltitude();
          direction = location.getBearing();
          MainActivity.toilets.add(this);
    }

   public ToiletLocation() {
   }

   public ToiletLocation(double latitude, double longitude, double altitude, float direction) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.altitude = altitude;
      this.direction = direction;
   }

   public double getLatitude() {
      return latitude;
   }

   public void setLatitude(double latitude) {
      this.latitude = latitude;
   }

   public double getLongitude() {
      return longitude;
   }

   public void setLongitude(double longitude) {
      this.longitude = longitude;
   }

   public double getAltitude() {
      return altitude;
   }

   public void setAltitude(double altitude) {
      this.altitude = altitude;
   }

   public float getDirection() {
      return direction;
   }

   public void setDirection(float direction) {
      this.direction = direction;
   }
}
