"""Google Gemini's native GenerateContent REST schema, kept separate from OpenAI-compatible APIs."""
from __future__ import annotations

import re
import urllib.parse


def endpoint(base: str, model: str) -> str:
    return base.rstrip("/") + "/models/" + urllib.parse.quote(model, safe="") + ":generateContent"


def _parts(content) -> list[dict]:
    if isinstance(content, str):
        return [{"text": content}]
    out = []
    if not isinstance(content, list):
        return out
    for item in content:
        if not isinstance(item, dict):
            continue
        if item.get("type") == "text" and isinstance(item.get("text"), str):
            out.append({"text": item["text"]})
        elif item.get("type") == "input_audio" and isinstance(item.get("input_audio"), dict):
            audio = item["input_audio"]
            data = audio.get("data")
            if not isinstance(data, str):
                continue
            mime = re.match(r"^data:([^;,]+)", data)
            out.append({"inlineData": {"mimeType": mime.group(1) if mime else "audio/" + audio.get("format", "wav"),
                                        "data": data.partition(",")[2] if data.startswith("data:") else data}})
    return out


def request(chat: dict) -> dict:
    contents, system = [], []
    for message in chat.get("messages", []):
        if not isinstance(message, dict):
            continue
        parts = _parts(message.get("content"))
        if message.get("role") in ("system", "developer"):
            system.extend(parts)
        elif parts:
            contents.append({"role": "model" if message.get("role") == "assistant" else "user", "parts": parts})
    result = {"contents": contents}
    if system:
        result["systemInstruction"] = {"parts": system}
    generation = {}
    if "temperature" in chat:
        generation["temperature"] = chat["temperature"]
    if "max_tokens" in chat:
        generation["maxOutputTokens"] = chat["max_tokens"]
    if (chat.get("response_format") or {}).get("type") == "json_object":
        generation["responseMimeType"] = "application/json"
    if generation:
        result["generationConfig"] = generation
    return result


def response(root: dict) -> dict:
    candidates = root.get("candidates")
    candidate = candidates[0] if isinstance(candidates, list) and candidates and isinstance(candidates[0], dict) else None
    if candidate is None:
        return {}
    content = candidate.get("content") if isinstance(candidate.get("content"), dict) else {}
    parts = content.get("parts", [])
    text = "".join(p.get("text", "") for p in parts if isinstance(p, dict) and p.get("thought") is not True
                   and isinstance(p.get("text"), str))
    finish = {"MAX_TOKENS": "length", "SAFETY": "content_filter", "RECITATION": "content_filter",
              "BLOCKLIST": "content_filter", "PROHIBITED_CONTENT": "content_filter"}.get(candidate.get("finishReason"), "stop")
    usage_meta = root.get("usageMetadata")
    usage = None
    if isinstance(usage_meta, dict):
        usage = {dst: usage_meta[src] for src, dst in (("promptTokenCount", "prompt_tokens"),
                                                        ("candidatesTokenCount", "completion_tokens"),
                                                        ("totalTokenCount", "total_tokens"))
                 if isinstance(usage_meta.get(src), int)}
    result = {"choices": [{"message": {"content": text}, "finish_reason": finish}]}
    if usage:
        result["usage"] = usage
    return result
