import { ImageViewer } from '../components/ImageViewer';
import { Overlay } from '../components/Overlay';

export function createApp(){
  const root = document.getElementById('app')!;
  root.innerHTML = `
  <main class="page">
    <header class="header">
      <div class="title-row">
        <h1>FlamEdgeViewer</h1>
        <span class="badge">Preview</span>
      </div>
      <p class="subtitle">Static processed frame from Android — preview, FPS & resolution overlay</p>
    </header>
    <section class="viewer-shell">
      <aside class="controls">
        <div class="control-card">
          <div class="label">FPS:</div>
          <div id="fps" class="value">—</div>
        </div>
        <div class="control-card">
          <div class="label">Resolution:</div>
          <div id="resolution" class="value">—</div>
        </div>
      </aside>
      <div class="image-area">
        <div id="image-container" class="image-container" aria-label="processed-frame"></div>
      </div>
    </section>
    <footer class="footer">Made by Mohit Singh — FlamEdgeViewer</footer>
  </main>
  `;

  const viewer = new ImageViewer('image-container');
  const overlay = new Overlay('fps','resolution');

  (async ()=>{
    try {
      await viewer.load('/sample-frame.png');
    } catch (e1) {
      try {
        await viewer.load('/sample-frame.jpg');
      } catch (e2) {
        const c = document.getElementById('image-container')!;
        c.textContent = 'Could not load sample-frame.png or .jpg — put your frame in public/';
        console.error('Failed to load sample frame', e1, e2);
        return;
      }
    }
    const r = viewer.getResolution();
    if(r) overlay.setResolution(r.width, r.height);
    setInterval(()=> overlay.tickFrame(), 1000/30);
  })();
}
