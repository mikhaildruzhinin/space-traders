const es = new EventSource("/events");

const setText = (selector, value) => {
    const el = document.querySelector(selector);
    if (el) el.textContent = value ?? "-";
}

es.onmessage = (e) => {
    try {
        const msg = JSON.parse(e.data);
        if (msg.type === "agent") {
            const agent = msg.agent ?? msg.data;
            if (!agent) {
                console.warn("Agent data is missing")
            }

            setText("[data-agent-id]", agent.id);
            setText("[data-agent-symbol]", agent.symbol);
            setText("[data-agent-hq]", agent.headquarters);
            setText("[data-agent-credits]", agent.credits);
            setText("[data-agent-faction]", agent.faction);
            setText("[data-agent-ship-count]", agent.shipCount);
        }
    } catch (err) {
        console.error("Bad SSE payload", err, e.data);
    }
}

es.onerror = (err) => {
    console.error("SSE error", err);
}
