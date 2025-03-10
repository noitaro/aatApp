/* TODO: Change toolbox XML ID if necessary. Can export toolbox XML from Workspace Factory. */
const toolbox = document.getElementById("toolbox");

const options = {
	toolbox: toolbox,
	collapse: true,
	comments: true,
	disable: true,
	maxBlocks: Infinity,
	trashcan: true,
	horizontalLayout: false,
	toolboxPosition: 'start',
	css: true,
	media: 'media/',
	rtl: false,
	scrollbars: true,
	sounds: false,
	oneBasedIndex: false,
	grid: {
		spacing: 20,
		length: 1,
		colour: '#888',
		snap: false
	},
	zoom: {
		controls: true,
		wheel: true,
		startScale: 1,
		maxScale: 3,
		minScale: 0.3,
		scaleSpeed: 1.2
	}
};

/* Inject your workspace */
const workspace = Blockly.inject('blocklyDiv', options);

/* Load Workspace Blocks from XML to workspace. Remove all code below if no blocks to load */

/* TODO: Change workspace blocks XML ID if necessary. Can export workspace blocks XML from Workspace Factory. */
const workspaceBlocks = document.getElementById("workspaceBlocks");

/* Load blocks to workspace. */
Blockly.Xml.domToWorkspace(workspaceBlocks, workspace);

console.log('Loaded');
