package info.jtrac.wicket;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

public class ImagePanel extends Panel {

	public ImagePanel(String id, ResourceReference image) {
		super(id);
		add(new Image("image", image));
	}


}
