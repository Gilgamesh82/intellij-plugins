// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirPackagingOptions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AirDescriptorForm {
  private JPanel myMainPanel; // required for form reuse
  private JRadioButton myGeneratedDescriptorRadioButton;
  private JRadioButton myCustomDescriptorRadioButton;
  private TextFieldWithBrowseButton myCustomDescriptorTextWithBrowse;
  private JButton myCreateDescriptorButton;

  public AirDescriptorForm(final Project project, final Runnable descriptorCreator) {
    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        if (myCustomDescriptorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(project).requestFocus(myCustomDescriptorTextWithBrowse.getTextField(), true);
        }
      }
    };

    myGeneratedDescriptorRadioButton.addActionListener(listener);
    myCustomDescriptorRadioButton.addActionListener(listener);

    myCustomDescriptorTextWithBrowse.addBrowseFolderListener(project, FlexUtils.createFileChooserDescriptor("xml"));

    myCreateDescriptorButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        descriptorCreator.run();
      }
    });
  }

  void updateControls() {
    myCustomDescriptorTextWithBrowse.setEnabled(myCustomDescriptorRadioButton.isEnabled() && myCustomDescriptorRadioButton.isSelected());
    myCreateDescriptorButton.setEnabled(myCustomDescriptorRadioButton.isEnabled() && myCustomDescriptorRadioButton.isSelected());
  }

  public void resetFrom(final AirPackagingOptions packagingOptions) {
    myGeneratedDescriptorRadioButton.setSelected(packagingOptions.isUseGeneratedDescriptor());
    myCustomDescriptorRadioButton.setSelected(!packagingOptions.isUseGeneratedDescriptor());
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath()));
  }

  public boolean isModified(final ModifiableAirPackagingOptions packagingOptions) {
    if (packagingOptions.isUseGeneratedDescriptor() != myGeneratedDescriptorRadioButton.isSelected()) return true;
    if (!packagingOptions.getCustomDescriptorPath().equals(
      FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()))) {
      return true;
    }

    return false;
  }

  public void applyTo(final ModifiableAirPackagingOptions model) {
    model.setUseGeneratedDescriptor(myGeneratedDescriptorRadioButton.isSelected());
    model.setCustomDescriptorPath(FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()));
  }

  public void setUseCustomDescriptor(final String descriptorPath) {
    myCustomDescriptorRadioButton.setSelected(true);
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(descriptorPath));
    updateControls();
  }

  public ActionCallback navigateTo(final AirPackagingConfigurableBase.Location location) {
    if (location == AirPackagingConfigurableBase.Location.CustomDescriptor) {
      return IdeFocusManager.findInstance().requestFocus(myCustomDescriptorTextWithBrowse.getChildComponent(), true);
    }
    return ActionCallback.DONE;
  }
}
