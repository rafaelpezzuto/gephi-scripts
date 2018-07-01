import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.layout.plugin.openord.OpenOrdLayoutBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.awt.*;
import java.io.File;

class Main {

    private ProjectController projectController;
    private Workspace workspace;
    private ImportController importController;
    private Container container;
    private GraphModel graphModel;
    private AppearanceController appearanceController;
    private AppearanceModel appearanceModel;

    private void startProject() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        workspace = projectController.getCurrentWorkspace();
        importController = Lookup.getDefault().lookup(ImportController.class);
    }

    private void importGraph(File graphFile) {
        try {
            container = importController.importFile(graphFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        importController.process(container, new DefaultProcessor(), workspace);
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        appearanceModel = appearanceController.getModel();
    }

    private void setOpenOrdPartitions(String columnName) {
        Column column = graphModel.getNodeTable().getColumn(columnName);
        Function partitionAttributeFunction = appearanceModel.getNodeFunction(graphModel.getDirectedGraph(), column, PartitionElementColorTransformer.class);
        Partition partition = ((PartitionFunction) partitionAttributeFunction).getPartition();

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

        appearanceController.transform(partitionAttributeFunction);
    }

    private void runOpenOrd(Integer numInteractions) {
        OpenOrdLayout ooLayout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        ooLayout.setGraphModel(graphModel);
        ooLayout.resetPropertiesValues();
        ooLayout.setNumIterations(numInteractions);
        ooLayout.setEdgeCut(0.9F);
        ooLayout.getBuilder().buildLayout();
        ooLayout.initAlgo();

        for (int i = 0; i < 2 * ooLayout.getNumIterations() & ooLayout.canAlgo(); i++) {
            ooLayout.goAlgo();
        }
        ooLayout.endAlgo();
    }

    private void saveProject(File outFile) {
        projectController.saveProject(projectController.getCurrentProject(), outFile).run();
    }


    public static void main(String args[]) {
        String graphFile = args[0];
        Integer numInteractions = Integer.valueOf(args[1]);
        String columnName = args[3];
        String outFile = args[4];

        Main m = new Main();
        m.startProject();
        m.importGraph(new File(graphFile));
        m.setOpenOrdPartitions(columnName);
        m.runOpenOrd(numInteractions);
        m.saveProject(new File(outFile));
    }
}
