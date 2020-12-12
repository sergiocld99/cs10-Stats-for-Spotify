package cs10.apps.web.statsforspotify.view.label;

import cs10.apps.desktop.statsforspotify.model.Status;
import cs10.apps.desktop.statsforspotify.utils.OldIOUtils;
import cs10.apps.web.statsforspotify.utils.CommonUtils;

import javax.swing.*;

public class CustomThumbnail extends JLabel {
    private final int size;

    public CustomThumbnail(int size) {
        this.size = size;
    }

    public void setCover(String imageUrl){
        this.setIcon(CommonUtils.downloadImage(imageUrl, size));
    }

    @Override
    public void setIcon(Icon icon) {
        super.setIcon(icon);
    }

    public void setUnknown(){
        this.setIcon(OldIOUtils.getImageIcon(Status.NOTHING));
    }
}
