import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {

        Map<String, Boolean> map = new HashMap<>();
        map.put("skipPublicite", true);
        map.put("skipDCE", true);
        map.put("skipRegistreDepot", true);
        map.put("skipQuestionReponse", true);
        map.put("skipRegistreRetrait", false);
        map.put("skipCandidature", true);
        map.put("skipRecommendation", true);
        map.put("skipCalendrierReel", false);
        map.put("skipSuiviEchange", true);

        if (args[0].endsWith("/")) {
            File[] jbpmFiles = new File(args[0]).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml") && !name.startsWith(".");
                }
            });

            if (jbpmFiles != null) {
                for (File file : jbpmFiles) {
                    SvgGenerator svgGenerator = new SvgGenerator(file.getPath(), map);
                    svgGenerator.createSvgOutput();
                }
            }
        } else if (args[0].endsWith(".xml")){
            File jbpmFile = new File(args[0]);
            SvgGenerator svgGenerator = new SvgGenerator(jbpmFile.getPath(), map);
            svgGenerator.createSvgOutput();
        }

    }
}
