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

public class ReportRendererPage extends BasePage {

	private WebComponent report;
	private HashMap map = new HashMap();
	
	private String reportFormat;

	@SuppressWarnings("unchecked")
	public ReportRendererPage(String startDate, String endDate, Space space, String reportName, String format) {
		
		this.reportFormat = format;
		
		map.put("StartDate", Date.valueOf(startDate));
		map.put("EndDate", Date.valueOf(endDate));
		
		map.put("Client", String.valueOf(space.getId()));
		
		String path = getJtrac().loadConfig("jtrac.path.birt.reports");
		
		String lastChar = path.substring(path.length() - 1);
		
		if(lastChar.equals("\\") || lastChar.equals("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		ReportRendererPage.this.generateReport(map, path + "/" + reportName);
		
		add(new DocumentInlineFrame("report", new MyPdfResource(path + "/" + reportName)));
		add(new WebMarkupContainer("reportjsp"));
	}
	
	private String stripExtension(String fileName, String extensionToBeStriped) {
		String result = fileName.replace("." + extensionToBeStriped, "");
		return result;
	}

	private class MyPdfResource extends DynamicWebResource {

		private static final long serialVersionUID = 1L;

		static final int BUFFER_SIZE = 10 * 1024;

		String fileName = "";

		/**
	     *
	     */
		public MyPdfResource(String fName) {
			this.fileName = stripExtension(fName, "rptdesign");
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.wicket.markup.html.DynamicWebResource#getResourceState()
		 */
		@Override
		protected ResourceState getResourceState() {
			return new ResourceState() {

				@Override
				public String getContentType() {
					if(reportFormat.toLowerCase().equals("xls")) {
						return "application/vnd.ms-excel";
					} else if(reportFormat.toLowerCase().equals("doc")) {
						return "application/msword";
					} else if(reportFormat.toLowerCase().equals("html")) {
						return "text/html";
					} else {
						return "application/" + reportFormat;
					}
				}

				@Override
				public byte[] getData() {
					try {
						
						String path = fileName + "." + reportFormat;
						
						FileInputStream fileInputStream = new FileInputStream(path);
						
						
						//System.out.println(bytes(fileInputStream));
						
						return bytes(fileInputStream);
						
					} catch (Exception e) {
						return null;
					}
				}
			};
		}

		public byte[] bytes(InputStream is) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(is, out);
			return out.toByteArray();
		}

		public void copy(InputStream is, OutputStream os) throws IOException {
			byte[] buf = new byte[BUFFER_SIZE];
			while (true) {
				int tam = is.read(buf);
				if (tam == -1) {
					return;
				}
				os.write(buf, 0, tam);
			}
		}
	}

	private class DocumentInlineFrame extends WebMarkupContainer implements
			IResourceListener {
		private static final long serialVersionUID = 1L;

		private IResourceListener resourceListener;

		/**
		 * Constructor receiving an IResourceListener..
		 * 
		 * @param id
		 * @param resourceListener
		 */
		public DocumentInlineFrame(final String id,
				IResourceListener resourceListener) {
			super(id);
			this.resourceListener = resourceListener;
		}

		/**
		 * Gets the url to use for this link.
		 * 
		 * @return The URL that this link links to
		 */
		protected CharSequence getURL() {
			return urlFor(IResourceListener.INTERFACE);
		}

		/**
		 * Handles this frame's tag.
		 * 
		 * @param tag
		 *            the component tag
		 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
		 */
		@Override
		protected final void onComponentTag(final ComponentTag tag) {
			checkComponentTag(tag, "iframe");

			// Set href to link to this frame's frameRequested method
			CharSequence url = getURL();

			
			// generate the src attribute of the iframe
			tag.put("src", Strings.replaceAll(url, "&", "&amp;"));

			super.onComponentTag(tag);
		}

		@Override
		protected boolean getStatelessHint() {
			return false;
		}

		public void onResourceRequested() {
			this.resourceListener.onResourceRequested();
		}
	}

	@SuppressWarnings("serial")
	protected void generateReport(final Map map, final String strReport) {
		
		AbstractResourceStreamWriter writer = new AbstractResourceStreamWriter() {
			
			public void write(OutputStream os) {
				
				String strippedReportName = stripExtension(strReport, "rptdesign");
				
				/*String existingReportPath = ((WebApplication) ReportRendererPage.this
						.getApplication()).getServletContext().getRealPath(
						"WEB-INF/birt/" + strippedReportName + "." + reportFormat);*/
				
				String existingReportPath = strippedReportName + "." + reportFormat;
				
				
				File file = new File(existingReportPath);
				if(file.exists())
					file.delete();
				
				EngineConfig config = new EngineConfig();
				/*String path = ((WebApplication) ReportRendererPage.this
						.getApplication()).getServletContext().getRealPath(
						"WEB-INF/birt/ReportEngine");*/
				
				String path = getJtrac().loadConfig("jtrac.path.birt.engine");
				
				config.setEngineHome(path);
				/*path = ((WebApplication) ReportRendererPage.this
						.getApplication()).getServletContext().getRealPath(
						"WEB-INF/log");*/
				config.setLogConfig(path, Level.FINE);
				try {

					Platform.startup(config);
					IReportEngineFactory factory = (IReportEngineFactory) Platform
							.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
					IReportEngine engine = factory.createReportEngine(config);
					IReportRunnable report = engine.openReportDesign(strReport);

					IRunAndRenderTask task = engine
							.createRunAndRenderTask(report);
					task.setParameterValues(map);
					
					
					if(reportFormat.toLowerCase().equals("xls")) {
						EXCELRenderOption options = new EXCELRenderOption();
						options.setOutputFormat("xls");
						existingReportPath = existingReportPath.replace("\\", "/");
						existingReportPath = existingReportPath.replace("/", File.separator);
						
						options.setOutputFileName(existingReportPath);
						
						//options.setOption(EXCELRenderOption.OUTPUT_FILE_NAME, existingReportPath);

						System.out.println("#################### " + options.getOutputFileName());
						
						options.setOption(EXCELRenderOption.OFFICE_VERSION, true);
		                options.setOption(EXCELRenderOption.WRAPPING_TEXT, false);
						
						options.setOption(IRenderOption.HTML_PAGINATION, Boolean.TRUE );
						options.setOption(IRenderOption.EMITTER_ID, "org.eclipse.birt.report.engine.emitter.prototype.excel");
			            options.setOption( "ExcelEmitter.DEBUG", Boolean.TRUE);
			            options.setOption("ExcelEmitter.RemoveBlankRows", Boolean.FALSE);
						
						options.setOutputStream(os);
						task.setRenderOption(options);
					} else if(reportFormat.toLowerCase().equals("doc")) {
						IRenderOption options = new RenderOption();
						options.setOutputFormat(reportFormat);
						options.setOutputStream(os);
						task.setRenderOption(options);
					} else if(reportFormat.toLowerCase().equals("html")) {
						HTMLRenderOption options = new HTMLRenderOption();
						options.setOutputFormat(reportFormat);
						options.setOption(IRenderOption.HTML_PAGINATION, Boolean.TRUE );
			            options.setOption( "ExcelEmitter.DEBUG", Boolean.TRUE);
			            options.setOption("ExcelEmitter.RemoveBlankRows", Boolean.FALSE);
						options.setOutputStream(os);
						task.setRenderOption(options);
					} else {
						PDFRenderOption options = new PDFRenderOption();
						options.setOutputFormat(reportFormat);
						options.setOutputStream(os);
						task.setRenderOption(options);						
					}
					
					task.run();
					task.close();
				} catch (BirtException e) {					
					throw new RuntimeException(e);					
				}
			}

			public String getContentType() {
				if(reportFormat.toLowerCase().equals("xls")) {
					return "application/vnd.ms-excel";
				} else if(reportFormat.toLowerCase().equals("doc")) {
					return "application/msword";
				} else if(reportFormat.toLowerCase().equals("html")) {
					return "text/html";
				} else {
					return "application/" + reportFormat;
				}
			}
			
			
		};
		
		this.getRequestCycle().setRequestTarget(
				new ResourceStreamRequestTarget(writer));
		
		OutputStream baos = null;
		try {
			String strippedFileName = stripExtension(strReport, "rptdesign");
			
			baos = new FileOutputStream(strippedFileName + "." + reportFormat);
			//baos = new FileOutputStream(((WebApplication)ReportRendererPage.this.getApplication()).getServletContext().getRealPath("WEB-INF/birt/" + strippedFileName + "." + reportFormat));
			//baos = new FileOutputStream(strippedFileName + "." + reportFormat);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		writer.write(baos);
		
	}

}
