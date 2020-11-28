package cs10.apps.web.statsforspotify.model;

import java.util.ArrayList;
import java.util.Random;

public class CustomList<E> extends ArrayList<E> {
    private final Random random;

    public CustomList(){
        this.random = new Random();
    }

    public E getRandomElement(){
        return get(random.nextInt(size()));
    }
}
