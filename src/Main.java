import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.UniqueNodeSizeTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

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
            File file = new File("db/grafo_vis.gdf");
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
        for (Node n : graphModel.getDirectedGraph().getNodes()) {
            uniqueNodeSizeTransformer.transform(n);
        }

        Column grandeArea = graphModel.getNodeTable().getColumn("grandearea");
        Function grandeAreaFunction = appearanceModel.getNodeFunction(graphModel.getDirectedGraph(), grandeArea, PartitionElementColorTransformer.class);
        Partition partition = ((PartitionFunction) grandeAreaFunction).getPartition();
        partition.setColor("Ciencias Humanas", Color.decode("#984ea3"));
        partition.setColor("Ciencias da Saude", Color.decode("#ffff33"));
        partition.setColor("Ciencias Exatas E da Terra", Color.decode("#4daf4a"));
        partition.setColor("Ciencias Sociais Aplicadas", Color.decode("#ff7f00"));
        partition.setColor("Engenharias", Color.decode("#a65628"));
        partition.setColor("Ciencias Agrarias", Color.decode("#e41a1c"));
        partition.setColor("Ciencias Biologicas", Color.decode("#377eb8"));
        partition.setColor("Linguistica Letras E Artes", Color.decode("#f781bf"));
        partition.setColor("Outros", Color.decode("#999999"));
        partition.setColor("", Color.decode("#dddddd"));

        appearanceController.transform(grandeAreaFunction);

        OpenOrdLayout ooLayout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        ooLayout.setGraphModel(graphModel);
        ooLayout.resetPropertiesValues();
        ooLayout.setNumIterations(20000);
        ooLayout.initAlgo();

        for (int i = 0; i < 2 * ooLayout.getNumIterations() & ooLayout.canAlgo(); i++) {
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
            exportController.exportFile(new File("db/grafo_vis.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) {

        Main m = new Main();
        m.script();
    }


}
