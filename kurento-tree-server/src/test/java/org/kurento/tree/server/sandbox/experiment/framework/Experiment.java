/*
 * (C) Copyright 2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.kurento.tree.server.sandbox.experiment.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kurento.tree.client.TreeException;
import org.kurento.tree.server.debug.TreeManagerReportCreator;
import org.kurento.tree.server.kmsmanager.FakeFixedNKmsManager;
import org.kurento.tree.server.kmsmanager.KmsManager;
import org.kurento.tree.server.sandbox.experiment.usage.UsageSimulation;
import org.kurento.tree.server.treemanager.TreeManager;

public abstract class Experiment {

  private List<UsageSimulation> usageSimulations = new ArrayList<>();
  private List<TreeManagerCreator> treeManagerCreators = new ArrayList<>();
  private KmsManager kmsManager = new FakeFixedNKmsManager(4);

  public abstract void configureExperiment();

  protected void addUsageSimulation(UsageSimulation usageSimulation) {
    usageSimulations.add(usageSimulation);
  }

  protected void addTreeManagerCreator(TreeManagerCreator treeManagerCreator) {
    treeManagerCreators.add(treeManagerCreator);
  }

  protected void setKmsManager(KmsManager kmsManager) {
    this.kmsManager = kmsManager;
  }

  public void run() {

    configureExperiment();

    TreeManagerReportCreator reportCreator = new TreeManagerReportCreator(kmsManager, "Report");

    reportCreator.addText("KmsManager: " + kmsManager.getClass().getName());

    for (TreeManagerCreator treeManagerCreator : treeManagerCreators) {

      for (UsageSimulation simulation : usageSimulations) {

        reportCreator.addSection("Simulation " + simulation.getClass().getName());

        TreeManager treeManager = treeManagerCreator.createTreeManager(kmsManager);

        reportCreator.setTreeManager(treeManager);

        try {
          simulation.useTreeManager(reportCreator);
        } catch (TreeException e) {
          System.out.println(
              "Reached maximum tree capacity in TreeManager: " + treeManager.getClass().getName()
                  + " and UsageSimulation: " + simulation.getClass().getName());
          System.out.println(e.getClass().getName() + ":" + e.getMessage());
        }
      }
    }

    try {
      String reportPath = System.getProperty("user.home") + "/Data/Kurento/Tree";
      new File(reportPath).mkdirs();
      String experimentName = this.getClass().getSimpleName();
      String reportFilePath = reportPath + "/treereport_" + experimentName + ".html";
      reportCreator.createReport(reportFilePath);
      System.out.println("Report created in: " + reportFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
