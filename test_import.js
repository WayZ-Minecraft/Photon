import { JSDOM } from 'jsdom';

const dom = new JSDOM('<!DOCTYPE html><html><body><div id=\"appShell\"></div><template id=\"appTemplate\"></template></body></html>', {
  url: 'http://localhost'
});

global.window = dom.window;
global.document = dom.window.document;
global.navigator = dom.window.navigator;
global.localStorage = dom.window.localStorage;
global.sessionStorage = dom.window.sessionStorage;
global.HTMLElement = dom.window.HTMLElement;
global.Node = dom.window.Node;

try {
  await import('./src/main/resources/public/js/dashboard.js');
  console.log('IMPORT_SUCCESS');
} catch (err) {
  console.error('IMPORT_FAILURE');
  console.error(err);
  process.exit(1);
}
