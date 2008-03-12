package hudson.plugins.crap4j;

import hudson.model.AbstractBuild;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.crap4j.calculation.HealthBuilder;
import hudson.plugins.crap4j.chart.AbstractChartMaker;
import hudson.plugins.crap4j.chart.AreaChartMaker;
import hudson.plugins.crap4j.chart.ChartSeriesDefinition;
import hudson.plugins.crap4j.chart.CrapDataSet;
import hudson.plugins.crap4j.model.ProjectCrapBean;
import hudson.util.ChartUtil;

import java.io.IOException;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class Crap4JBuildAction implements StaplerProxy, HealthReportingAction {
	
	private static final long serialVersionUID = 8586323795728749743L;
	private final ProjectCrapBean crap;
	private final AbstractBuild<?, ?> build;
	
	public Crap4JBuildAction(AbstractBuild<?, ?> owner,
			ProjectCrapBean crap) {
		super();
		this.build = owner;
		this.crap = crap;
	}
	
	@Override
	public String getDisplayName() {
		return "Crap";
	}
	
	public ProjectCrapBean getCrap() {
		return this.crap;
	}
	
	@Override
	public String getIconFileName() {
		return "/plugin/crap4j/icons/crap-32x32.gif";
	}
	
	@Override
	public String getUrlName() {
		return "crapResult";
	}
	
	public CrapBuildResult getTarget() {
		return new CrapBuildResult(
				this.build,
				this.crap);
	}
	
	public CrapBuildResult getResult() {
		return getTarget();
	}
	
	public boolean hasPreviousCrap() {
		return (null != getCrap().getPrevious());
	}
	
	@Override
	public HealthReport getBuildHealth() {
		return (new HealthBuilder().getHealthReportFor(this.crap));
	}
	
    public final void doGraphMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        CrapDataSet dataset = new CrapDataSet(this.crap);
        ChartSeriesDefinition definition = getChartDefinitionFor(request);
        ChartUtil.generateClickableMap(request, response,
        		definition.getChartMaker().createChart(
        				dataset.buildCategoryDataSet(definition),
        				definition.getAxisTitle()),
        		500, 200);
    }
    
    public final void doGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        CrapDataSet dataset = new CrapDataSet(this.crap);
        ChartSeriesDefinition definition = getChartDefinitionFor(request);
        ChartUtil.generateGraph(request, response,
        		definition.getChartMaker().createChart(
        				dataset.buildCategoryDataSet(definition),
        				definition.getAxisTitle()),
        		500, 200);
    }
    
    private ChartSeriesDefinition getChartDefinitionFor(StaplerRequest request) {
    	String dataType = request.getParameter("data");
    	if ("crapMethodCount".equals(dataType)) {
    		return new ChartSeriesDefinition("crap methods", "method count") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getCrapMethodCount();
    			}
    			@Override
    			public AbstractChartMaker getChartMaker() {
    				return new AreaChartMaker();
    			}
    		};
    	}
    	if ("crapLoad".equals(dataType)) {
    		return new ChartSeriesDefinition("crap load", "crap load") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getCrapLoad();
    			}
    		};
    	}
    	if ("crap".equals(dataType)) {
    		return new ChartSeriesDefinition("crap", "crap amount") {
    			@Override
    			public Number extractNumberFrom(ProjectCrapBean crap) {
    				return crap.getTotalCrap();
    			}
    		};
    	}
    	/* default */
    	return new ChartSeriesDefinition("crappyness", "crap method percentage") {
    		@Override
    		public Number extractNumberFrom(ProjectCrapBean crap) {
    			return crap.getCrapMethodPercent();
    		}
    	};
    }
}