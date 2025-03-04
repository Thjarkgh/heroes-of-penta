import OpenAI from "openai";
import IOpenAiAdapter from "../service/IOpenAiAdapter";

export default class OpenAiAdapter implements IOpenAiAdapter {
  constructor(
    private readonly openai: OpenAI
  ) {}

  public static build(apiKey: string) {
    return new OpenAiAdapter(new OpenAI({ apiKey }));
  }

  async analyzeImage(query: string, image: Buffer) {
    const result = await this.openai.chat.completions.create({
      model: "gpt-4o-mini",
      messages: [
        {
          role: "user",
          content: [
            { type: "text", text: query },
            { type: "image_url", image_url: { url: `data:image/heic;base64,${image.toString("base64")}` } }
          ]
        }
      ],
      store: false // true to use for training a smaller, cheaper model
    });

    if (result.choices[0].message.refusal != null) {
      throw new Error(`Chat GPT refused to process request: ${result.choices[0].message.refusal}`);
    }

    let resultString = result.choices[0].message.content;
    if (resultString == null) {
      throw new Error(`Chat GPT did not return result`);
    }
    while (resultString.startsWith('`') && resultString.endsWith('`')) {
      resultString = resultString.substring(1, resultString.length - 1);
    }

    if (!resultString.startsWith("json")) {
      throw new Error(`Expected JSON result, got ${resultString.substring(0, 10)}`);
    }
    resultString = resultString.substring(4).trim();

    const resultObject = JSON.parse(resultString);
    return resultObject;
  }
}