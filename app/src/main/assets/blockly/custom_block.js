Blockly.Blocks['screen_capture'] = {
  init: function () {
    this.appendDummyInput()
      .appendField("アンドロイドのスクリーンショットを撮影");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
  }
};

Blockly.Blocks['tap'] = {
  init: function () {
    this.appendValueInput("NAME")
      .setCheck(null)
      .appendField("スクリーンショット内をタップ");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
  }
};

Blockly.Blocks['field_image_serializable'] = {
  init: function () {
    this.appendDummyInput()
      .appendField(new Blockly.FieldTextInput("default"), "NAME");
    this.setOutput(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['sleep'] = {
  init: function () {
    this.appendDummyInput()
      .appendField(new Blockly.FieldNumber(3, 0), "NAME")
      .appendField("秒間停止します");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['text_log'] = {
  init: function () {
    this.appendValueInput("TEXT")
      .setCheck("String")
      .appendField("log");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['device_tap'] = {
  init: function () {
    this.appendDummyInput()
      .appendField("tap");
    this.appendValueInput("X")
      .setCheck("Number")
      .setAlign(Blockly.ALIGN_RIGHT)
      .appendField("x");
    this.appendValueInput("Y")
      .setCheck("Number")
      .setAlign(Blockly.ALIGN_RIGHT)
      .appendField("y");
    this.appendDummyInput();
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['text_toast'] = {
  init: function () {
    this.appendValueInput("TEXT")
      .setCheck("String")
      .appendField("toast");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['device_key'] = {
  init: function () {
    this.appendDummyInput()
      .appendField("Press the ")
      .appendField(new Blockly.FieldDropdown([["home", "3"], ["back", "4"], ["camera", "27"], ["app switch", "187"], ["menu", "82"], ["power", "26"], ["settings", "176"], ["volume down", "25"], ["volume mute", "164"], ["volume up", "24"], ["screenshot", "120"]]), "NAME")
      .appendField("button");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['app_start'] = {
  init: function () {
    this.appendDummyInput()
      .appendField("アプリ起動");
    this.appendValueInput("NAME1")
      .setCheck("String")
      .appendField(new Blockly.FieldLabelSerializable("・パッケージ名"), "NAME1");
    this.appendValueInput("NAME2")
      .setCheck("String")
      .appendField(new Blockly.FieldLabelSerializable("・クラス名"), "NAME2");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['app_end'] = {
  init: function () {
    this.appendDummyInput()
      .appendField("アプリ終了");
    this.appendValueInput("NAME1")
      .setCheck(null)
      .appendField(new Blockly.FieldLabelSerializable("パッケージ名"), "NAME1");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};

Blockly.Blocks['image_existence_confirmation'] = {
  init: function () {
    this.appendValueInput("NAME1")
      .setCheck("TemplateImage")
      .appendField(new Blockly.FieldDropdown([["テンプレート画像が存在する", "true"], ["テンプレート画像が存在しない","false"]]), "NAME1");
    this.appendStatementInput("NAME2")
      .setCheck(null)
      .appendField("実行");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
    this.setTooltip("");
    this.setHelpUrl("");
  }
};
