export class ImageViewer {
  private container: HTMLElement;
  private imageEl: HTMLImageElement;

  constructor(containerId: string){
    const c = document.getElementById(containerId);
    if(!c) throw new Error(`Container #${containerId} not found`);
    this.container = c;
    this.container.innerHTML = '';
    this.imageEl = document.createElement('img');
    this.imageEl.alt = 'Processed frame';
    this.imageEl.style.width = '100%';
    this.imageEl.style.height = '100%';
    this.imageEl.style.objectFit = 'contain';
    this.container.appendChild(this.imageEl);
  }

  load(src: string): Promise<void> {
    return new Promise((resolve,reject)=>{
      this.imageEl.onload = ()=> resolve();
      this.imageEl.onerror = (e)=>{
        // Fallback: inline SVG placeholder (data URI) so the UI shows something immediately
        const placeholderSvg = encodeURIComponent(`
          <svg xmlns='http://www.w3.org/2000/svg' width='1200' height='800'>
            <rect width='100%' height='100%' fill='#0f1724'/>
            <text x='50%' y='50%' fill='#9fb4d6' font-family='Arial' font-size='28' text-anchor='middle'>
              Could not load sample-frame.png
            </text>
            <text x='50%' y='58%' fill='#9fb4d6' font-family='Arial' font-size='20' text-anchor='middle'>
              (put sample-frame.png in public/)
            </text>
          </svg>
        `);
        this.imageEl.src = `data:image/svg+xml;charset=utf-8,${placeholderSvg}`;
        reject(e);
      };
      this.imageEl.src = src;
    });
  }

  getResolution(){
    if(!this.imageEl || !this.imageEl.naturalWidth) return null;
    return { width: this.imageEl.naturalWidth, height: this.imageEl.naturalHeight };
  }
}
