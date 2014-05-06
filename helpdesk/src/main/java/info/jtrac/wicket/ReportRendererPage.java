package info.jtrac.wicket;

import info.jtrac.domain.Space;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.string.Strings;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.service.BirtReportServiceFactory;
import org.eclipse.birt.report.service.api.IViewerReportService;

public class ReportRendererPage extends BasePage {
	
	public ReportRendererPage(String reportName) {
		
		String jasperServerURL = getJtrac().loadConfig("jasper.server.url");
		String jasperServerUsername = getJtrac().loadConfig("jasper.server.username");
		String jasperServerPassword = getJtrac().loadConfig("jasper.server.password");
		
		String lastChar = jasperServerURL.substring(jasperServerURL.length() - 1);
		
		if(!lastChar.equals("\\") && !lastChar.equals("/")) {
			jasperServerURL = jasperServerURL + "/";
		}
		
		jasperServerURL = jasperServerURL + "flow.html?_flowId=viewReportFlow&standAlone=true&ParentFolderUri=%2Freports%2Finteractive&reportUnit=%2Freports%2Finteractive%2F" + reportName + "&decorate=no&j_username=" + jasperServerUsername 
						+ "&j_password=" + jasperServerPassword + "&user=" + getPrincipal().getId();
		
		
		add(new WebMarkupContainer("report").add(new SimpleAttributeModifier("src", jasperServerURL)));
		
	}
	


}
