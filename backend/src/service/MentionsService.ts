import z from "zod";
import IMentionRepository from "./IMentionRepository";
import Mention from "../domain/entities/mentionAggregate/Mention";

// sample tester message
/*{"object":"instagram","entry":[{"time":1740157200749,"id":"17841472110558136","messaging":[{"sender":{"id":"489760440872775"},"recipient":{"id":"17841472110558136"},"timestamp":1740157200421,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI3NjAyNTg0MzYzNjQ5MDY2MjozMjEwMDIzNDUyNDE5MjQ5ODg1NjAzOTM5MjY5NzM4NDk2MAZDZD","text":"Omg"}}]}]}*/

// sample tester image message
/*{"object":"instagram","entry":[{"time":1740157287033,"id":"17841472110558136","messaging":[{"sender":{"id":"489760440872775"},"recipient":{"id":"17841472110558136"},"timestamp":1740157286706,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI3NjAyNTg0MzYzNjQ5MDY2MjozMjEwMDIzNjExNTg4MzI3NzM3OTI0MTg2MTg1NjYyNDY0MAZDZD","attachments":[{"type":"image","payload":{"url":"https://lookaside.fbsbx.com/ig_messaging_cdn/?asset_id=1151970572602487&signature=AYddBE7nIoOZgwxUzmOcNvQdjj-29xgWns_TQqxig-qfUXUUXjn3FsBFrPGA9sDrgJPpxqhyESOuWSx8YDWN_2nDrVU1--ByBoPnPidHowPGtlqR_W5011Zjt0RbEvRwHJ4h8vDuIIB_qDVf9u3F63x5NtubhGLnBoXy2OhMJLXX_xALKGdIbrtjowK8u253rz5w_vqLJ6VR4d5jEMNjtzMvLNRvIFE"}}]}}]}]}*/

// sample message 3rd user
/*{"object":"instagram","entry":[{"time":1740157485698,"id":"17841472110558136","messaging":[{"sender":{"id":"1471737950450354"},"recipient":{"id":"17841472110558136"},"timestamp":1740157484343,"message":{"mid":"aWdfZAG1faXRlbToxOklHTWVzc2FnZAUlEOjE3ODQxNDcyMTEwNTU4MTM2OjM0MDI4MjM2Njg0MTcxMDMwMTI0NDI1OTc3MzQ2MzgxMTk3ODMwMDozMjEwMDIzOTc2MTYzMTUxNTQwMjQ4NDg3ODUzMzQ1OTk2OAZDZD","text":"Bla"}}]}]}*/

// mention sample:
/*{"entry":[{"id":"0","time":1740156774,"changes":[{"field":"mentions","value":{"media_id":"17887498072083520","comment_id":"17887498072083520"}}]}],"object":"instagram"}*/

// tag not working:
export const mentionData = z.object({
  object: z.literal("instagram"),
  entry: z.array(z.object({
    time: z.number(),
    id: z.string(),
    messaging: z.optional(z.array(z.object({
      sender: z.object({ id: z.string() }),
      recipient: z.object({ id: z.string() }),
      timestamp: z.number(),
      message: z.object({
        mid: z.string(),
        text: z.ostring(),
        attachments: z.optional(z.array(z.object({
          type: z.string(),
          payload: z.object({
            url: z.string()
          })
        })))
      })
    }))),
    changes: z.optional(z.object({
      field: z.literal("mentions"),
      value: z.object({
        media_id: z.string(),
        comment_id: z.string()
      })
    }).array())
  }))
});

export type MentionData = typeof mentionData._type;

export default class MentionsService {
  constructor(
    private readonly repo: IMentionRepository
  ) {}

  async handle(mention: MentionData) {
    const candidateEntries = mention.entry.filter((e) => (e.changes || []).find((c) => c.field === "mentions"));

    for (const entry of candidateEntries) {
      const changes = entry.changes!.filter((c) => c.field === "mentions").map((c) => c.value);
      for (const change of changes) {
        const m = new Mention(change.media_id, change.comment_id, entry.time);
        await this.repo.push(m);
      }
    }
  } 
}