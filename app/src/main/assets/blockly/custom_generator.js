Blockly.Lua['text_log'] = function(block) {
  var value_text = Blockly.Lua.valueToCode(block, 'TEXT', Blockly.Lua.ORDER_ATOMIC);
  // TODO: Assemble Lua into code variable.
  var code = 'MyLua2Java.log(' + value_text + ');\n';
  return code;
};

Blockly.Lua['controls_sleep'] = function(block) {
  var value_num = Blockly.Lua.valueToCode(block, 'NUM', Blockly.Lua.ORDER_ATOMIC);
  // TODO: Assemble Lua into code variable.
  var code = 'MyLua2Java.sleep(' + value_num + ');\n';
  return code;
};

Blockly.Lua['device_tap'] = function(block) {
  var value_x = Blockly.Lua.valueToCode(block, 'X', Blockly.Lua.ORDER_ATOMIC);
  var value_y = Blockly.Lua.valueToCode(block, 'Y', Blockly.Lua.ORDER_ATOMIC);
  // TODO: Assemble Lua into code variable.
  var code = 'MyLua2Java.deviceTap(' + value_x + ', ' + value_y + ');\n';
  return code;
};

Blockly.Lua['text_toast'] = function(block) {
  var value_text = Blockly.Lua.valueToCode(block, 'TEXT', Blockly.Lua.ORDER_ATOMIC);
  // TODO: Assemble Lua into code variable.
  var code = 'MyLua2Java.toast(' + value_text + ');\n';
  return code;
};

Blockly.Lua['device_key'] = function(block) {
  var dropdown_name = block.getFieldValue('NAME');
  // TODO: Assemble Lua into code variable.
  var code = 'MyLua2Java.deviceKey(' + dropdown_name + ');\n';
  return code;
};