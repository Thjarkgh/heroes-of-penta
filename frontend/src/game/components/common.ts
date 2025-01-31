
//changes 100px to 100
export const toNum = (s: number | string) => {
  s = s.toString().replace("px", "");
  s = parseInt(s);
  return s;
}