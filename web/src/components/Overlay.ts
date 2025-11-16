export class Overlay {
  private fpsEl: HTMLElement;
  private resEl: HTMLElement;
  private frames = 0;

  constructor(fpsId: string, resId: string){
    this.fpsEl = document.getElementById(fpsId)!;
    this.resEl = document.getElementById(resId)!;
    setInterval(()=>{ this.fpsEl.textContent = `FPS: ${this.frames}`; this.frames = 0; },1000);
  }

  tickFrame(){ this.frames++; }
  setResolution(w:number,h:number){ this.resEl.textContent = `Resolution: ${w} × ${h}`; }
}
