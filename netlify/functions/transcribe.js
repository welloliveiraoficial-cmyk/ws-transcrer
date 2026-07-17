// Chama o generateContent do Gemini usando a chave secreta do servidor.
// Faz retentativa automática em caso de sobrecarga (503), respeitando o
// tempo limite de execução da função.
export default async (req) => {
  if (req.method !== 'POST') {
    return new Response('Method not allowed', { status: 405 });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return Response.json(
      { error: 'O servidor ainda não tem a chave de API configurada (GEMINI_API_KEY).' },
      { status: 500 }
    );
  }

  try {
    const { fileUri, mimeType } = await req.json();
    if (!fileUri || !mimeType) {
      return Response.json({ error: 'fileUri e mimeType são obrigatórios.' }, { status: 400 });
    }

    const promptTexto = 'Transcreva integralmente o áudio deste arquivo. Detecte o idioma automaticamente e escreva a transcrição no idioma original falado, com pontuação e parágrafos naturais. Responda apenas com o texto transcrito, sem comentários, sem introdução e sem marcar o idioma.';

    // Só uma retentativa curta aqui dentro — funções serverless têm um
    // tempo de execução limitado, então retentativas mais longas ficam
    // por conta do navegador (que chama esta função de novo se preciso).
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
                { file_data: { mime_type: mimeType, file_uri: fileUri } },
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
          return Response.json({ error: 'A API não retornou nenhum texto. O arquivo pode não conter fala reconhecível.' }, { status: 502 });
        }
        return Response.json({ text });
      }

      const apiMsg = data?.error?.message || 'Erro desconhecido na API.';
      const sobrecarregado = resp.status === 503 || apiMsg.toLowerCase().includes('overloaded') || apiMsg.toLowerCase().includes('high demand');

      if (sobrecarregado && tentativa < MAX_TENTATIVAS) {
        await new Promise((r) => setTimeout(r, ESPERA_MS));
        continue;
      }

      return Response.json(
        { error: sobrecarregado ? 'servidor_sobrecarregado' : apiMsg },
        { status: sobrecarregado ? 503 : resp.status }
      );
    }
  } catch (e) {
    return Response.json({ error: e.message || 'Erro interno na transcrição.' }, { status: 500 });
  }
};
