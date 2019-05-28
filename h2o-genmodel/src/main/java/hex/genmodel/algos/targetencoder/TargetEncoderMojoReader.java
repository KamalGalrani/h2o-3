package hex.genmodel.algos.targetencoder;

import hex.genmodel.ModelMojoReader;
import hex.genmodel.algos.word2vec.Word2VecMojoModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetEncoderMojoReader extends ModelMojoReader<TargetEncoderMojoModel> {

  @Override
  public String getModelName() {
    return "TargetEncoder";
  }

  @Override
  protected void readModelData() throws IOException {
    _model._targetEncodingMap = parseTargetEncodingMap("feature_engineering/target_encoding.ini");
  }

  @Override
  protected TargetEncoderMojoModel makeModel(String[] columns, String[][] domains, String responseColumn) {
    return new TargetEncoderMojoModel(columns, domains, responseColumn);
  }

  public Map<String, Map<String, int[]>> parseTargetEncodingMap(String pathToSource) throws IOException {
    Map<String, Map<String, int[]>> encodingMap = null;

    if(getMojoReaderBackend().exists(pathToSource)) {
      BufferedReader source = getMojoReaderBackend().getTextFile("feature_engineering/target_encoding.ini");

      encodingMap = new HashMap<>();
      Map<String, int[]> encodingsForColumn = null;
      String sectionName = null;
      try {
        String line;

        while (true) {
          line = source.readLine();
          if (line == null) { // EOF
            encodingMap.put(sectionName, encodingsForColumn);
            break;
          }
          line = line.trim();
          if (sectionName == null) {
            sectionName = matchNewSection(line);
            encodingsForColumn = new HashMap<>();
          } else {
            String matchResult = matchNewSection(line);
            if (matchResult != null) {
              encodingMap.put(sectionName, encodingsForColumn);
              encodingsForColumn = new HashMap<>();
              sectionName = matchResult;
              continue;
            }

            String[] res = line.split("\\s*=\\s*", 2);
            int[] numAndDenom = processNumeratorAndDenominator(res[1].split(" "));
            encodingsForColumn.put(res[0], numAndDenom);
          }
        }
        source.close();
      } finally {
        try {
          source.close();
        } catch (IOException e) { /* ignored */ }
      }
    }
    return encodingMap;
  }


  private String matchNewSection(String line) {
    Pattern pattern = Pattern.compile("\\[(.*?)\\]");
    Matcher matcher = pattern.matcher(line);
    if (matcher.find()) {
      return matcher.group(1);
    } else return null;
  }

  private int[] processNumeratorAndDenominator(String[] strings) {
    int[] intArray = new int[strings.length];
    int i = 0;
    for (String str : strings) {
      intArray[i] = Integer.parseInt(str);
      i++;
    }
    return intArray;
  }

  @Override public String mojoVersion() {
    return "1.00";
  }

}
