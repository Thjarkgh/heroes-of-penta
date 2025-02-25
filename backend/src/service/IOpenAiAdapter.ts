export default interface IOpenAiAdapter {
  analyzeImage(query: string, image: Buffer): Promise<any>;
}