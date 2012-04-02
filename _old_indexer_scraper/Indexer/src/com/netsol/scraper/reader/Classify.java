package com.netsol.scraper.reader;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Hasnain Rashid</p>
 *
 * <p>Company: Netsol Technologies</p>
 *
 * @author not attributable
 * @version 1.0
 */


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package org.apache.mahout.classifier;

import java.util.List;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.bayes.algorithm.BayesAlgorithm;
import org.apache.mahout.classifier.bayes.algorithm.CBayesAlgorithm;
import org.apache.mahout.classifier.bayes.common.BayesParameters;
import org.apache.mahout.classifier.bayes.datastore.HBaseBayesDatastore;
import org.apache.mahout.classifier.bayes.datastore.InMemoryBayesDatastore;
import org.apache.mahout.classifier.bayes.interfaces.Algorithm;
import org.apache.mahout.classifier.bayes.interfaces.Datastore;
import org.apache.mahout.classifier.bayes.model.ClassifierContext;
import org.apache.mahout.classifier.bayes.exceptions.InvalidDatastoreException;
import org.apache.mahout.common.nlp.NGrams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the Bayes classifier using the given model location(HDFS/HBASE)
 *
 */
public final class Classify {

  private static final Logger log = LoggerFactory.getLogger(Classify.class);

  public Classify() { }

  public static void main(String[] args) throws Exception {

    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();

    Option pathOpt = obuilder.withLongName("path").withRequired(true).withArgument(
      abuilder.withName("path").withMinimum(1).withMaximum(1).create()).withDescription(
      "The local file system path").withShortName("m").create();

    Option classifyOpt = obuilder.withLongName("classify").withRequired(true).withArgument(
      abuilder.withName("classify").withMinimum(1).withMaximum(1).create()).withDescription(
      "The doc to classify").withShortName("c").create();

    Option encodingOpt = obuilder.withLongName("encoding").withRequired(true).withArgument(
      abuilder.withName("encoding").withMinimum(1).withMaximum(1).create()).withDescription(
      "The file encoding.  Default: UTF-8").withShortName("e").create();

    Option analyzerOpt = obuilder.withLongName("analyzer").withRequired(false).withArgument(
      abuilder.withName("analyzer").withMinimum(1).withMaximum(1).create()).withDescription(
      "The Analyzer to use").withShortName("a").create();

    Option defaultCatOpt = obuilder.withLongName("defaultCat").withRequired(true).withArgument(
      abuilder.withName("defaultCat").withMinimum(1).withMaximum(1).create()).withDescription(
      "The default category").withShortName("d").create();

    Option gramSizeOpt = obuilder.withLongName("gramSize").withRequired(true).withArgument(
      abuilder.withName("gramSize").withMinimum(1).withMaximum(1).create()).withDescription(
      "Size of the n-gram").withShortName("ng").create();

    Option typeOpt = obuilder.withLongName("classifierType").withRequired(true).withArgument(
      abuilder.withName("classifierType").withMinimum(1).withMaximum(1).create()).withDescription(
      "Type of classifier").withShortName("type").create();

    Option dataSourceOpt = obuilder.withLongName("dataSource").withRequired(true).withArgument(
      abuilder.withName("dataSource").withMinimum(1).withMaximum(1).create()).withDescription(
      "Location of model: hdfs|hbase").withShortName("source").create();

    Group options = gbuilder.withName("Options").withOption(pathOpt).withOption(classifyOpt).withOption(
      encodingOpt).withOption(analyzerOpt).withOption(defaultCatOpt).withOption(gramSizeOpt).withOption(
      typeOpt).withOption(dataSourceOpt).create();

    Parser parser = new Parser();
    parser.setGroup(options);
    CommandLine cmdLine = parser.parse(args);

  }
    //public String classifyItem(String pathOpt, String classifyOpt, String encodingOpt, String analyzerOpt, String defaultCatOpt, String gramSizeOpt, String typeOpt, String dataSourceOpt, String text)
    public String classifyItem(String pathOpt, String encodingOpt, String analyzerOpt, String defaultCatOpt, String gramSizeOpt, String typeOpt, String dataSourceOpt, String text)
    {

    int gramSize = 1;
    if (gramSizeOpt!=null) {
      gramSize = Integer.parseInt((String) gramSizeOpt);

    }

    BayesParameters params = new BayesParameters(gramSize);
    params.set("basePath", (String)(pathOpt));

    String modelBasePath = (String)(pathOpt);

    log.info("Loading model from: {}", params.print());
    System.out.println("Loading model from: {}" + params.print());

    System.out.println("model base path: " + modelBasePath);
    //System.out.println("file path: " + (String)(classifyOpt));

    Algorithm algorithm;
    Datastore datastore;

    String classifierType = (String)(typeOpt);

    String dataSource = (String)(dataSourceOpt);
    if ("hdfs".equals(dataSource)) {
      if ("bayes".equalsIgnoreCase(classifierType)) {
        log.info("Using Bayes Classifier");
        algorithm = new BayesAlgorithm();
        datastore = new InMemoryBayesDatastore(params);
      } else if ("cbayes".equalsIgnoreCase(classifierType)) {
        log.info("Using Complementary Bayes Classifier");
        algorithm = new CBayesAlgorithm();
        datastore = new InMemoryBayesDatastore(params);
      } else {
        throw new IllegalArgumentException("Unrecognized classifier type: " + classifierType);
      }

    } else if ("hbase".equals(dataSource)) {
      if ("bayes".equalsIgnoreCase(classifierType)) {
        log.info("Using Bayes Classifier");
        algorithm = new BayesAlgorithm();
        datastore = new HBaseBayesDatastore(modelBasePath, params);
      } else if ("cbayes".equalsIgnoreCase(classifierType)) {
        log.info("Using Complementary Bayes Classifier");
        algorithm = new CBayesAlgorithm();
        datastore = new HBaseBayesDatastore(modelBasePath, params);
      } else {
        throw new IllegalArgumentException("Unrecognized classifier type: " + classifierType);
      }

    } else {
      throw new IllegalArgumentException("Unrecognized dataSource type: " + dataSource);
    }
    ClassifierContext classifier = new ClassifierContext(algorithm, datastore);
    try
    {
        classifier.initialize();
    }
    catch (InvalidDatastoreException exp)
    {
        exp.printStackTrace();
    }
    String defaultCat = "unknown";
    if (defaultCatOpt!=null && !defaultCat.isEmpty()) {
      defaultCat = defaultCatOpt;
    }
    //File docPath = new File(classifyOpt);
    String encoding = "UTF-8";
    if (encodingOpt!=null && !encodingOpt.isEmpty()) {
      encoding = encodingOpt;
    }
    Analyzer analyzer = null;
    if (analyzerOpt!=null && analyzerOpt.isEmpty()) {
      String className = analyzerOpt;
        try
        {
            analyzer = Class.forName(className).asSubclass(Analyzer.class).newInstance();
        }
        catch (Exception exp)
        {
            exp.printStackTrace();
        }
    }
    if (analyzer == null) {
      analyzer = new StandardAnalyzer(Version.LUCENE_29);
    }

    /*log.info("Converting input document to proper format");
        String[] document = null;
        try
        {
            document = org.apache.mahout.classifier.BayesFileFormatter.readerToDocument(analyzer, new InputStreamReader(
        new FileInputStream(docPath), Charset.forName(encoding)));
        }
        catch(Exception exp)
        {
            exp.printStackTrace();
        }
    StringBuilder line = new StringBuilder();
    for (String token : document) {
      line.append(token).append(' ');
    }*/

    List<String> doc = new NGrams(text.toString(), gramSize).generateNGramsWithoutLabel();

    log.info("Done converting");
    //log.info("Classifying document: {}", docPath);
    log.info("Classifying text: {}", text);
    System.out.println("Default category: " + defaultCat);
        org.apache.mahout.classifier.ClassifierResult category=null;
                try
                {
                     category = classifier.classifyDocument(doc.toArray(new String[doc.size()]), defaultCat);
                }
                catch (Exception exp)
                {
                    exp.printStackTrace();
                }
    System.out.println(category);
    log.info("Category for {} is {}", text, category);


        return category.getLabel().toString();

  }
}
