// Chama o generateContent do Gemini usando a chave secreta do servidor.
// Recebe o arquivo em base64 (dados "inline") em vez de fazer o navegador
// enviar o arquivo direto pro Google — o Google bloqueia esse tipo de
// upload direto do navegador por segurança (CORS), então o arquivo passa
// por aqui. Por isso o tamanho do arquivo fica limitado pelo tamanho
// máximo de requisição da função (alguns MB).
export default async function handler(req, res) {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    res.status(500).json({ error: 'O servidor ainda não tem a chave de API configurada (GEMINI_API_KEY).' });
    return;
  }

  try {
    const { mimeType, base64Data } = req.body || {};
    if (!mimeType || !base64Data) {
      res.status(400).json({ error: 'mimeType e base64Data são obrigatórios.' });
      return;
    }

    const promptTexto = 'Transcreva integralmente o áudio deste arquivo. Detecte o idioma automaticamente e escreva a transcrição no idioma original falado, com pontuação e parágrafos naturais. Responda apenas com o texto transcrito, sem comentários, sem introdução e sem marcar o idioma.';

    const MAX_TENTATIVAS = 2;
    const ESPERA_MS = 2500;

    for (let tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
      const resp = await fetch(
        'https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=' + encodeURIComponent(apiKey),
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{
              parts: [
                { inline_data: { mime_type: mimeType, data: base64Data } },
                { text: promptTexto }
              ]
            }]
          })
        }
      );

      const data = await resp.json();

      if (resp.ok) {
        const candidate = data.candidates && data.candidates[0];
        const parts = candidate && candidate.content && candidate.content.parts;
        const text = parts && parts.map((p) => p.text || '').join('').trim();
        if (!text) {
          res.status(502).json({ error: 'A API não retornou nenhum texto. O arquivo pode não conter fala reconhecível.' });
          return;
        }
        res.status(200).json({ text });
        return;
      }

      const apiMsg = data?.error?.message || 'Erro desconhecido na API.';
      const sobrecarregado = resp.status === 503 || apiMsg.toLowerCase().includes('overloaded') || apiMsg.toLowerCase().includes('high demand');

      if (sobrecarregado && tentativa < MAX_TENTATIVAS) {
        await new Promise((r) => setTimeout(r, ESPERA_MS));
        continue;
      }

      res.status(sobrecarregado ? 503 : resp.status).json({ error: sobrecarregado ? 'servidor_sobrecarregado' : apiMsg });
      return;
    }
  } catch (e) {
    res.status(500).json({ error: e.message || 'Erro interno na transcrição.' });
  }
}
