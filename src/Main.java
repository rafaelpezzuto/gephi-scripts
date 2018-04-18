import com.itextpdf.text.PageSize;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.UniqueNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
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

    private void setNodeSize(Integer size) {
        UniqueNodeSizeTransformer uniqueNodeSizeTransformer = new UniqueNodeSizeTransformer();
        uniqueNodeSizeTransformer.setSize(size);
        for (Node n : graphModel.getDirectedGraph().getNodes()) {
            uniqueNodeSizeTransformer.transform(n);
        }
    }

    private void setOpenOrdPartitions(String columnName) {
        Column column = graphModel.getNodeTable().getColumn(columnName);
        Function grandeAreaFunction = appearanceModel.getNodeFunction(graphModel.getDirectedGraph(), column, PartitionElementColorTransformer.class);
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
    }

    private void runOpenOrd(Integer numInteractions) {
        OpenOrdLayout ooLayout = new OpenOrdLayout(new OpenOrdLayoutBuilder());
        ooLayout.setGraphModel(graphModel);
        ooLayout.resetPropertiesValues();
        ooLayout.setNumIterations(numInteractions);
        ooLayout.initAlgo();

        for (int i = 0; i < 2 * ooLayout.getNumIterations() & ooLayout.canAlgo(); i++) {
            ooLayout.goAlgo();
        }
        ooLayout.endAlgo();
    }

    private void exportLayoutPDF(File outFile) {
        ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        PDFExporter pdfExporter = (PDFExporter) exportController.getExporter("pdf");
        pdfExporter.setPageSize(PageSize.A0);
        pdfExporter.setWorkspace(workspace);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportController.exportStream(baos, pdfExporter);

        try {
            exportController.exportFile(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        String graphFile = args[0];
        Integer numInteractions = Integer.valueOf(args[1]);
        Integer nodeSize = Integer.valueOf(args[2]);
        String columnName = args[3];
        String outFile = args[4];

        Main m = new Main();
        m.startProject();
        m.importGraph(new File(graphFile));
        m.setNodeSize(nodeSize);
        m.setOpenOrdPartitions(columnName);
        m.runOpenOrd(numInteractions);
        m.exportLayoutPDF(new File(outFile));
    }
}
