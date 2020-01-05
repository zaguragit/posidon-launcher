package posidon.launcher.tools;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;

public class ThemeTools {

	public static float getScaleFactor(Resources res, String string) {
		float scaleFactor = 1.0f;
		XmlResourceParser xrp = null;
		XmlPullParser xpp = null;
		try {
			int n;
	        if ((n = res.getIdentifier("appfilter", "xml", string)) != 0)
	        	xrp = res.getXml(n);
	        else {
	            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	            factory.setValidating(false);
	            xpp = factory.newPullParser();
	            InputStream raw = res.getAssets().open("appfilter.xml");
	            xpp.setInput(raw, null);
	        }

	        if (n!=0) {
		        while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && scaleFactor==1.0f) {
					if (xrp.getEventType()==2) {
						try {
							String s = xrp.getName();
							if (s.equals("scale")) scaleFactor = Float.parseFloat(xrp.getAttributeValue(0));
						} catch(Exception ignore) {}
					} xrp.next();
		        }
	        } else {
	        	while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && scaleFactor==1.0f) {
	                if (xpp.getEventType()==2) {
	                	try {
							String s = xpp.getName();
							if (s.equals("scale")) scaleFactor = Float.parseFloat(xpp.getAttributeValue(0));
	                	} catch(Exception ignore) {}
	                } xpp.next();
	        	}
	        }
		} catch(Exception ignore) {}
		return scaleFactor;
	}

	public static String getResourceName(Resources res, String iconPack, String componentInfo) {
		String resource = null;
		XmlResourceParser xrp = null;
		XmlPullParser xpp = null;
		try {
			int n;
			if ((n = res.getIdentifier("appfilter", "xml", iconPack)) != 0) xrp = res.getXml(n);
			else {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setValidating(false);
				xpp = factory.newPullParser();
				InputStream raw = res.getAssets().open("appfilter.xml");
				xpp.setInput(raw, null);
			}

			if (n != 0) {
				while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && resource == null) {
					if (xrp.getEventType() == 2) {
						try {
							if (xrp.getName().equals("item") && xrp.getAttributeValue(0).compareTo(componentInfo)==0)
								resource = xrp.getAttributeValue(1);
						} catch(Exception ignore){}
					}
					xrp.next();
				}
			} else {
				while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && resource == null) {
					if (xpp.getEventType() == 2) {
						try {
							if (xpp.getName().equals("item") && xpp.getAttributeValue(0).compareTo(componentInfo) == 0)
								resource = xpp.getAttributeValue(1);
						} catch(Exception ignore) {}
					}
					xpp.next();
				}
			}
		} catch(Exception ignore) {}
		return resource;
	}

	public static ArrayList<String> getResourceNames(Resources res, String iconPack) {
		XmlResourceParser xrp = null;
		XmlPullParser xpp = null;
		ArrayList<String> strings = new ArrayList<>();
		try {
			int n;
			if ((n = res.getIdentifier("appfilter", "xml", iconPack)) != 0) xrp = res.getXml(n);
			else {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setValidating(false);
				xpp = factory.newPullParser();
				InputStream raw = res.getAssets().open("appfilter.xml");
				xpp.setInput(raw, null);
			}

			if (n != 0) {
				while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
					try {
						if (xrp.getEventType() == 2 && !strings.contains(xrp.getAttributeValue(1)))
							if (xrp.getName().equals("item")) strings.add(xrp.getAttributeValue(1));
					} catch (Exception ignore) {}
					xrp.next();
				}
			} else {
				while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
					try {
						if (xpp.getEventType() == 2 && !strings.contains(xpp.getAttributeValue(1)))
							if (xpp.getName().equals("item")) strings.add(xpp.getAttributeValue(1));
					} catch (Exception ignore) {}
					xpp.next();
				}
			}
		} catch(Exception ignore) {}
		return strings;
	}

	public static String [] getIconBackAndMaskResourceName(Resources res, String packageName) {
		String[] resource = new String[3];
		XmlResourceParser xrp = null;
		XmlPullParser xpp = null;
		try {
			int n;
			if ((n = res.getIdentifier("appfilter", "xml", packageName)) != 0) xrp = res.getXml(n);
			else {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setValidating(false);
				xpp = factory.newPullParser();
				InputStream raw = res.getAssets().open("appfilter.xml");
				xpp.setInput(raw, null);
			}

			if (n != 0) {
				while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT && (resource[0] == null || resource[1] == null || resource[2] == null)) {
					if (xrp.getEventType() == 2) {
						try { switch (xrp.getName()) {
								case "iconback":
									resource[0] = xrp.getAttributeValue(0);
									break;
								case "iconmask":
									resource[1] = xrp.getAttributeValue(0);
									break;
								case "iconupon":
									resource[2] = xrp.getAttributeValue(0);
									break;
						}} catch(Exception ignore) {}
					} xrp.next();
				}
			} else {
				while (xpp.getEventType() != XmlPullParser.END_DOCUMENT && (resource[0] == null || resource[1] == null || resource[2] == null)) {
					if (xpp.getEventType() == 2) {
						try { switch (xpp.getName()) {
								case "iconback":
									resource[0] = xpp.getAttributeValue(0);
									break;
								case "iconmask":
									resource[1] = xpp.getAttributeValue(0);
									break;
								case "iconupon":
									resource[2] = xpp.getAttributeValue(0);
									break;
						}} catch(Exception ignore) {}
					} xpp.next();
				}
			}
		} catch(Exception ignore) {}
		return resource;
	}
}