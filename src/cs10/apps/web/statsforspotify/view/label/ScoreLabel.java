package cs10.apps.web.statsforspotify.view.label;

public class ScoreLabel extends CircleLabel {
    private static final String DEFAULT_LABEL = "Artist Score";
    private static final String SPECIAL_LABEL = "Collab Score";

    public ScoreLabel(){
        super(DEFAULT_LABEL, false);
        setVisible(false);
    }

    public void setCollab(boolean isCollab){
        if (isCollab) super.setTitle(SPECIAL_LABEL);
        else super.setTitle(DEFAULT_LABEL);
    }
}
