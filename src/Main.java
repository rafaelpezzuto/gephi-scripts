import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.UniqueNodeSizeTransformer;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.jfree.ui.Size2D;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.xml.crypto.dsig.Transform;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

class Main {

    public void script() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        try {
            File file = new File("/home/rafael/Projects/gephi-scripts/db/grafo.gdf");
            container = importController.importFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        importController.process(container, new DefaultProcessor(), workspace);

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        UniqueNodeSizeTransformer uniqueNodeSizeTransformer = new UniqueNodeSizeTransformer();
        uniqueNodeSizeTransformer.setSize(45);
        for (Node n: graphModel.getDirectedGraph().getNodes()) {
            uniqueNodeSizeTransformer.transform(n);
        }

        Column grandeArea = graphModel.getNodeTable().getColumn("grande_area");
        Function grandeAreaFunction = appearanceModel.getNodeFunction(graphModel.getDirectedGraph(), grandeArea, PartitionElementColorTransformer.class);

        OpenOrdLayout ooLayout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        ooLayout.setGraphModel(graphModel);
        ooLayout.resetPropertiesValues();
        ooLayout.setNumIterations(750);
        ooLayout.initAlgo();

        for (int i = 0; i < 1500 & ooLayout.canAlgo(); i++) {
            ooLayout.goAlgo();
        }
        ooLayout.endAlgo();

        ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        PNGExporter pngExporter = (PNGExporter) exportController.getExporter("png");
        pngExporter.setWorkspace(workspace);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pngExporter.setOutputStream(baos);
        pngExporter.execute();

        try {
            exportController.exportFile(new File("/home/rafael/Projects/gephi-scripts/db/grafo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) {

        Main m = new Main();
        m.script();
    }


}
