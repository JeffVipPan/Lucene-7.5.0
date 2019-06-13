package lucene.index;

import io.FileOperation;
import io.NativeFSLockFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Lu Xugang
 * @date 2019-02-21 09:58
 */
public class IndexFileWithManyFieldValues {
  private Directory directory = new ByteBuffersDirectory();
  private Directory directory2;
  private Directory directory3;

//  {
//    try {
//      FileOperation.deleteFile("./data");
//      FileOperation.deleteFile("./data1");
//      directory3 = FSDirectory.open(Paths.get("./data"));
//      directory2 = FSDirectory.open(Paths.get("./data1"));
//      Set<String> primaryExtensions = new HashSet<>();
//      primaryExtensions.add("fdx");
//      primaryExtensions.add("fdt");
//      primaryExtensions.add("nvd");
//      primaryExtensions.add("nvm");
//      directory = new FileSwitchDirectory(primaryExtensions, directory3, directory2, true);
//      directory = FSDirectory.open(Paths.get("./data"));
//      directory = new NIOFSDirectory(Paths.get("./data"));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }

  private Analyzer analyzer = new WhitespaceAnalyzer();
  private IndexWriterConfig conf = new IndexWriterConfig(analyzer);
  private IndexWriter indexWriter;

  public void doIndex() throws Exception {

    FieldType type = new FieldType();
    type.setStored(true);
    type.setStoreTermVectors(true);
    type.setStoreTermVectorPositions(true);
    type.setStoreTermVectorPayloads(true);
    type.setStoreTermVectorOffsets(true);
    type.setTokenized(true);
    type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

    conf.setUseCompoundFile(false);
    conf.setMergePolicy(NoMergePolicy.INSTANCE);
    conf.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
    conf.setSoftDeletesField("myDeleteFiled");
    indexWriter = new IndexWriter(directory, conf);

    int count = 0;
    int n = 0;
    Document doc ;
    while (count++ < 20) {
//      doc.add(new Field("content", "abc", type));
//      doc.add(new Field("content", "cd", type));
//      doc.add(new StoredField("content", 3));
//      doc.add(new Field("author", "efg", type));

      // 文档0
      doc = new Document();
      doc.add(new NumericDocValuesField("abc", 0));
      doc.add(new Field("content", "a", type));
      doc.add(new IntPoint("abc", 3, 5, 9));
      indexWriter.addDocument(doc);

      // 文档1
      doc = new Document();
      doc.add(new SortedDocValuesField("forSort", new BytesRef("a")));
      doc.add(new Field("content", "b", type));
      doc.add(new IntPoint("abc", 3, 9, 9));
      indexWriter.addDocument(doc);

      // 文档2
      doc = new Document();
      doc.add(new NumericDocValuesField("abc", 0));
      doc.add(new Field("content", "c", type));
      indexWriter.addDocument(doc);

//      indexWriter.updateDocValues(new Term("content", "c"), new NumericDocValuesField("文档2", 3));
//      indexWriter.updateDocValues(new Term("content", "a"), new NumericDocValuesField("文档0", 4));

      // 文档3
      doc = new Document();
      doc.add(new Field("content", "d", type));
      doc.add(new BinaryDocValuesField("myDocValues", new BytesRef("d")));
      indexWriter.addDocument(doc);
//      indexWriter.flush();
//      indexWriter.updateDocValues(new Term("content", "d"), new BinaryDocValuesField("文档3", new BytesRef("e")));

     if(count % 10 == 0){
       indexWriter.commit();
     }

    }
    Map<String, String> userData = new HashMap<>();
    userData.put("1", "abc");
    userData.put("2", "efd");
    indexWriter.setLiveCommitData(userData.entrySet());
    indexWriter.commit();

    DirectoryReader  reader = DirectoryReader.open(indexWriter);
    IndexSearcher searcher = new IndexSearcher(reader);


    Query query = new TermQuery(new Term("content", "a"));
    ScoreDoc[] scoreDocs = searcher.search(query, 10).scoreDocs;

    Document document  = reader.document(2);
    System.out.println(document.get("content"));

    // Per-top-reader state:
  }

  public static String getSamePrefixRandomValue(String prefix){
    String str="abcdefghijklmnopqrstuvwxyz";
    Random random=new Random();
    StringBuffer sb=new StringBuffer();
    int length = getLength();
    for(int i=0;i<length;i++){
      int number=random.nextInt(25);
      sb.append(prefix);
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

  public static String getRandomValue(){
    String str="abcdefghijklmnopqrstuvwxyz";
    Random random=new Random();
    StringBuffer sb=new StringBuffer();
    int length = getLength();
    for(int i=0;i<length;i++){
      int number=random.nextInt(25);
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

  public static int getLength(){
    Random random = new Random();
    int length = random.nextInt(5);
    if (length < 3){
      length = length + 3;
    }
    return length;
  }

  public static String getMultiSamePrefixValue(String prefix, int wordNum){
    int valueCount = 0;
    StringBuilder stringBuilder = new StringBuilder();
    while (valueCount++ < wordNum){
      stringBuilder.append(getSamePrefixRandomValue(prefix));
      stringBuilder.append(" ");
    }
    stringBuilder.append("end");
    return stringBuilder.toString();
  }

  public static String getMultiValue(){
    int valueCount = 0;
    StringBuilder stringBuilder = new StringBuilder();
    while (valueCount++ < 99){
      stringBuilder.append(getRandomValue());
      stringBuilder.append(" ");
    }
    stringBuilder.append("end");
    return stringBuilder.toString();
  }

  public static void main(String[] args) throws Exception{
    IndexFileWithManyFieldValues test = new IndexFileWithManyFieldValues();
    test.doIndex();
  }
}
