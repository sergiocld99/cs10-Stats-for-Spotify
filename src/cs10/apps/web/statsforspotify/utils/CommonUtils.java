package cs10.apps.web.statsforspotify.utils;

import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

public class CommonUtils {

    public static String combineArtists(ArtistSimplified[] arr){
        StringBuilder sb = new StringBuilder(arr[0].getName());
        for (int i=1; i<arr.length; i++) sb.append(", ").append(arr[i].getName());
        return sb.toString();
    }

}
