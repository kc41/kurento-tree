package org.kurento.tree.server.kms.loadmanager;

import org.kurento.tree.server.kms.Kms;
import org.kurento.tree.server.kms.Pipeline;

public class MaxWebRtcLoadManager implements LoadManager {

  private int maxWebRtcPerKms;

  public MaxWebRtcLoadManager(int maxWebRtcPerKms) {
    this.maxWebRtcPerKms = maxWebRtcPerKms;
  }

  @Override
  public double calculateLoad(Kms kms) {
    int numWebRtcs = countWebRtcEndpoints(kms);
    if (numWebRtcs > maxWebRtcPerKms) {
      return 1;
    } else {
      return numWebRtcs / ((double) maxWebRtcPerKms);
    }
  }

  private int countWebRtcEndpoints(Kms kms) {
    int numWebRtcs = 0;
    for (Pipeline pipeline : kms.getPipelines()) {
      numWebRtcs += pipeline.getWebRtcs().size();
      numWebRtcs += pipeline.getPlumbers().size();
    }
    return numWebRtcs;
  }

  @Override
  public boolean allowMoreElements(Kms kms) {
    return countWebRtcEndpoints(kms) < maxWebRtcPerKms;
  }
}
