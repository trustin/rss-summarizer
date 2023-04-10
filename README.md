# Add summary and translation to an RSS feed

This project loads an RSS feed, summarizes each entry (blog post or podcast)
into a list of key takeaways (using [Kagi Universal Summarizer](https://kagi.com/summarizer/)),
translates the original content of the post (using [DeepL](https://www.deepl.com/))
and generates the new feed with summary and translation.

Various configurable properties are hard-coded in `Main.kt`, the entry
point of this project:

- API keys:
  - Kagi Universal Summarizer: `~/APIKeys/kagi_key.txt`
  - DeepL: `~/APIKeys/deepl_key.txt`.
- Cache directory: `~/.cache/rss-summarizer/`
- Output file: `summary_feed.rss`
- Target language: `KO` (Korean)

Run `Main.kt` with an RSS feed URL as the first argument.

## Future work

- Make the whole project configurable from a configuration file
  such as `~/.config/rss-summarizer/config.toml`.
- Allow processing more than one RSS feed.
- Use OpenAI Whisper to transcribe a podcast for more accurate summarization.
- Fetch the content of a blog post using Postlight Reader when the feed doesn't
  provide the full text.
