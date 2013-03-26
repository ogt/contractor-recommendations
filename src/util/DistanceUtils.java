package util;

public class DistanceUtils
{

    // Half the circumference of the earth is the maximum distance in 
    // mileage between any two points on earth
    public static final double MAX_DISTANCE_MILEAGE = 24901.55 / 2;
    
    /**
     * mean radius of the earth (in miles)
     */
    public static final double EARTH_MEAN_RADIUS = 3958.75;

    /**
     * @deprecated <b>Use getLatLongDistance instead.</b>
     * Returns the number of miles between lat1/long1 and lat2/long2. This is a 
     * simplistic implementation that doesn't take the curvature of the 
     * earth into account -- so it's fine for short distances.
     */
    public static double simpleLatLongMileage(double lat1, double long1, double lat2, double long2)
    {
        if(lat1 == lat2 && long1 == long2) {
            return 0.0;
        }
        double x = 69.1 * (lat2 - lat1);
        double y = 69.1 * (long2 - long1) * 
                   Math.cos(lat1/57.3);
        return Math.sqrt(x * x + y * y);
    }

    /**
     * @deprecated <b>Use getLatLongDistance instead.</b>
     * Returns the number of miles between lat1/long1 and lat2/long2. This is a 
     * more exact implementation that takes the curvature of the 
     * earth into account -- so it's better for longer distances.
     */
    public static double latLongMileage(double lat1, double long1, double lat2, double long2)
    {
        if(lat1 == lat2 && long1 == long2) {
            return 0.0;
        }
        return 3958.75 * 
            Math.acos(
                    Math.sin(lat1/57.2958) *
                    Math.sin(lat2/57.2958) +
                    Math.cos(lat1/57.2958) *
                    Math.cos(lat2/57.2958) *
                    Math.cos(long2/57.2958 - long1/57.2958)
                    );
    }

        
    public static double getLatLongDistanceKM(double lat1, double long1, double lat2, double long2) {
        return 1.609344 * getLatLongDistance(lat1, long1, lat2, long2);
    }

    /**
     * Calculates the distance (in miles) between two lat/long points using the Haversine formula 
     * (more accurate than the law of cosines version for short distances) 
     * http://mathforum.org/library/drmath/view/51879.html
     * http://williams.best.vwh.net/avform.htm#Dist
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     * @return
     */
    public static double getLatLongDistance(double lat1, double long1, double lat2, double long2) {
        
        double dlon = Math.toRadians(long2-long1);
        double dlat = Math.toRadians(lat2-lat1);
        double a = Math.pow(Math.sin(dlat/2D),2) + 
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dlon/2D),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return EARTH_MEAN_RADIUS * c;
        
    }
    
    /**
     * Calculates the maximum latitude difference over a specified distance
     * @param dist
     * @return max latitude difference from traveling <pre>dist</pre> miles due north or south
     */
    public static double getLatitudeRange(double dist) {
        double d = dist/EARTH_MEAN_RADIUS;  //simple calc since latitude is concentric
        return Math.toDegrees(d);
    }
    
    /**
     * Calculates the maximum longitude difference over a specified distance and starting point
     * This is different from the latitude calculation since longitudinal lines are curved.
     * 
     * @param lat - latitude of starting point in degrees
     * @param lon - longitude of starting point in degrees
     * @param dist - distance in miles
     * @return - max longitude difference from traveling <pre>dist</pre> miles due east or west
     */
    public static double getLongitudeRange(double lat, double lon, double dist) {
        double d = dist/EARTH_MEAN_RADIUS;
        // calculate coordinate going straight east, then find longitude difference
        return Math.abs(getCoordWithDistanceAngle(lat,lon,d,Math.PI/2D)[1]-lon);
    }
    
    /**
     * Gets the lat/long of a point given a starting point, distance, and direction  
     * http://mathforum.org/library/drmath/view/51816.html
     * http://williams.best.vwh.net/avform.htm#LL
     * 
     * @param lat1 - latitude of starting point
     * @param long1 - longitude of starting point
     * @param r - distance in radians
     * @param theta - direction in radians
     * @return array of 2 doubles (lat/long) 
     */
    public static double[] getCoordWithDistanceAngle(double lat1, double long1, double dist, double theta) {
        double lat1Rad = Math.toRadians(lat1);
        double long1Rad = Math.toRadians(long1);
        double retLat = Math.asin(Math.sin(lat1Rad)*Math.cos(dist)+Math.cos(lat1Rad)*Math.sin(dist)*Math.cos(theta));
        double dlon = Math.atan2(Math.sin(theta)*Math.sin(dist)*Math.cos(lat1Rad),Math.cos(dist)-Math.sin(lat1Rad)*Math.sin(retLat));
        double retLong= (long1Rad+dlon+Math.PI)%(2*Math.PI)-Math.PI;
        return new double[]{Math.toDegrees(retLat),Math.toDegrees(retLong)};
    }
}
