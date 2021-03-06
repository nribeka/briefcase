/*
 * Copyright (C) 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.briefcase.ui.reused.transfer.sourcetarget.target;

import static org.opendatakit.briefcase.model.form.FormMetadataQueries.submissionVersionsOf;
import static org.opendatakit.briefcase.ui.reused.UI.makeClickable;
import static org.opendatakit.briefcase.ui.reused.UI.uncheckedBrowse;

import java.awt.Container;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JLabel;
import org.bushe.swing.event.EventBus;
import org.opendatakit.briefcase.model.BriefcasePreferences;
import org.opendatakit.briefcase.model.FormStatus;
import org.opendatakit.briefcase.model.form.FileSystemFormMetadataAdapter;
import org.opendatakit.briefcase.model.form.FormKey;
import org.opendatakit.briefcase.model.form.FormMetadataPort;
import org.opendatakit.briefcase.push.PushEvent;
import org.opendatakit.briefcase.push.central.PushToCentral;
import org.opendatakit.briefcase.reused.BriefcaseException;
import org.opendatakit.briefcase.reused.http.Http;
import org.opendatakit.briefcase.reused.job.JobsRunner;
import org.opendatakit.briefcase.reused.transfer.CentralServer;
import org.opendatakit.briefcase.reused.transfer.RemoteServer.Test;
import org.opendatakit.briefcase.transfer.TransferForms;
import org.opendatakit.briefcase.ui.reused.transfer.sourcetarget.CentralServerDialog;

/**
 * Represents an ODK Central server as a target for sending forms for the Push UI Panel.
 */
public class Central implements PushTarget<CentralServer> {
  private final Http http;
  private final Test<CentralServer> serverTester;
  private final Consumer<PushTarget> onSourceCallback;
  private CentralServer server;

  Central(Http http, Test<CentralServer> serverTester, Consumer<PushTarget> onSourceCallback) {
    this.http = http;
    this.serverTester = serverTester;
    this.onSourceCallback = onSourceCallback;
  }

  @Override
  public void storeTargetPrefs(BriefcasePreferences prefs, boolean storePasswords) {
    server.storeInPrefs(prefs, storePasswords);
  }

  @Override
  public JobsRunner push(TransferForms forms, Path briefcaseDir) {
    forms.filter(FormStatus::isEncrypted)
        .forEach(form -> form.setStatusString("Skipping. Encrypted forms can't be pushed to ODK Central yet"));

    String token = http.execute(server.getSessionTokenRequest()).orElseThrow(() -> new BriefcaseException("Can't authenticate with ODK Central"));
    PushToCentral pushOp = new PushToCentral(http, server, briefcaseDir, token, EventBus::publish);

    return JobsRunner
        .launchAsync(forms.filter(f -> !f.isEncrypted()).map(pushOp::push))
        .onComplete(() -> EventBus.publish(new PushEvent.Complete()));
  }

  @Override
  public String getDescription() {
    return server.getBaseUrl().toString();
  }

  @Override
  public Optional<String> getPushWarning(Path briefcaseDir, TransferForms selectedForms) {
    FormMetadataPort formMetadataPort = FileSystemFormMetadataAdapter.at(briefcaseDir);

    Set<String> namesOfMultiVersionForms = new HashSet<>();

    selectedForms.forEach(form -> {
      Set<String> knownVersions = formMetadataPort.query(submissionVersionsOf(FormKey.from(form)));
      form.getVersion().ifPresent(knownVersions::add);
      if (knownVersions.size() > 1) {
        namesOfMultiVersionForms.add(form.getFormName());
      }
    });

    if (namesOfMultiVersionForms.size() > 0) {
      String formNames = String.join(", ", namesOfMultiVersionForms);
      return Optional.of("Some forms (" + formNames + ") have submissions for multiple versions. However, Briefcase only " +
          "has the latest form definition for each.<br/<br/>If you proceed, Briefcase will push the same form definition " +
          "for all versions not already on Central. Alternately, you can cancel and manually upload form definitions to Central. " +
          "The active form definition version must be uploaded last.<br/><br/>It is generally safe to proceed. See " +
          "https://docs.getodk.org/central-briefcase to understand the implications.</a>");
    }
    return Optional.empty();
  }

  @Override
  public void onSelect(Container container) {
    CentralServerDialog dialog = CentralServerDialog.empty(serverTester);
    dialog.onConnect(this::set);
    dialog.getForm().setVisible(true);
  }

  @Override
  public void decorate(JLabel label) {
    label.setText("" +
        "<html>" +
        "URL: <a style=\"text-decoration: none\" href=\"" + server.getBaseUrl().toString() + "\">" + getDescription() + "</a><br/>" +
        "Project ID: " + server.getProjectId() + "" +
        "</html>");
    makeClickable(label, () -> uncheckedBrowse(server.getBaseUrl()));
  }

  @Override
  public boolean canBeReloaded() {
    return false;
  }

  @Override
  public boolean accepts(Object o) {
    return o instanceof CentralServer;
  }

  @Override
  public void set(CentralServer server) {
    this.server = server;
    onSourceCallback.accept(this);
  }

  @Override
  public String toString() {
    return "Central server";
  }
}
