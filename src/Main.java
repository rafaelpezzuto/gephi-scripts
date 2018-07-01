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

    private void runOpenOrd(Integer numInteractions, Float edgeCut, Integer liquid, Integer expansion, Integer cooldown, Integer crunch, Integer simmer) {
        OpenOrdLayout ooLayout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        ooLayout.setGraphModel(graphModel);
        ooLayout.resetPropertiesValues();

        ooLayout.setNumIterations(numInteractions);
        ooLayout.setEdgeCut(edgeCut);
        ooLayout.setLiquidStage(liquid);
        ooLayout.setExpansionStage(expansion);
        ooLayout.setCooldownStage(cooldown);
        ooLayout.setCrunchStage(crunch);
        ooLayout.setSimmerStage(simmer);

        ooLayout.getBuilder().buildLayout();
        ooLayout.initAlgo();

        System.out.println("CHECKING OPEN-ORD SETTINGS");
        System.out.println("--------------------------");
        System.out.println(" Iteractions " + ooLayout.getNumIterations());
        System.out.println(" EdgeCut " + ooLayout.getEdgeCut());
        System.out.println(" Liquid " + ooLayout.getLiquidStage());
        System.out.println(" Expansion " + ooLayout.getExpansionStage());
        System.out.println(" Cooldown " + ooLayout.getCooldownStage());
        System.out.println(" Crunch " + ooLayout.getCrunchStage());
        System.out.println(" Simmer " + ooLayout.getSimmerStage());
        System.out.println("--------------------------");

        for (int i = 0; i < 2 * ooLayout.getNumIterations() & ooLayout.canAlgo(); i++) {
            ooLayout.goAlgo();
        }
        ooLayout.endAlgo();
    }

    private void saveProject(File outFile) {
        projectController.saveProject(projectController.getCurrentProject(), outFile).run();
    }


    public static void main(String args[]) {
        String inGDFFile = args[0];
        String partitionAttribute = args[1];

        Integer numInteractions = Integer.valueOf(args[2]);
        Float edgeCut = Float.valueOf(args[3]);
        Integer liquid = Integer.valueOf(args[4]);
        Integer expansion = Integer.valueOf(args[5]);
        Integer cooldown = Integer.valueOf(args[6]);
        Integer crunch = Integer.valueOf(args[7]);
        Integer simmer = Integer.valueOf(args[8]);

        String outGephiFile = args[9];

        Main m = new Main();
        m.startProject();
        m.importGraph(new File(inGDFFile));
        m.setOpenOrdPartitions(partitionAttribute);
        m.runOpenOrd(numInteractions, edgeCut, liquid, expansion, cooldown, crunch, simmer);
        m.saveProject(new File(outGephiFile));
    }
}
