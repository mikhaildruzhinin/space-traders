const es = new EventSource("/events");

const setText = (selector, val) => {
    const el = document.querySelector(selector);
    if (el) el.textContent = val ?? "-";
};

const bind = (map, data) => {
    Object.entries(map).forEach(([selector, key]) => {
        setText(selector, data?.[key]);
    })
};

const handlers = {
    status: (data) => bind({
        "[data-status]": "status",
    }, data),
    agent: (data) => bind({
        "[data-agent-id]": "id",
        "[data-agent-symbol]": "symbol",
        "[data-agent-hq]": "headquarters",
        "[data-agent-credits]": "credits",
        "[data-agent-faction]": "faction",
        "[data-agent-ship-count]": "shipCount",
    }, data),
    contract: (data) => bind({
        "[data-contract-id]": "id",
    }, data),
};

es.onmessage = (e) => {
    try {
        const msg = JSON.parse(e.data);

        if (!msg || typeof msg.type !== "string") {
            console.warn("Event missing type", msg);
            return;
        }

        const handler = handlers[msg.type];
        if (!handler) {
            console.warn("Unhandled event type", msg.type, msg);
            return;
        }

        if (msg.data == null) {
            console.warn("Event missing data", msg.type, msg);
            return;
        }

        handler(msg.data);
    } catch (err) {
        console.error("Bad SSE payload", err, e.data);
    }
};

es.onerror = (err) => {
    console.error("SSE error", err);
};
