// Inicia uma sessão de upload em etapas (File API do Gemini).
// A chave de API nunca sai do servidor: só devolvemos ao navegador a URL
// de upload já autorizada, e o arquivo em si vai direto do navegador pro
// Google (não passa pela nossa função, evitando o limite de tamanho dela).
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
    const { fileName, mimeType, fileSize } = await req.json();

    if (!fileSize) {
      return Response.json({ error: 'fileSize é obrigatório.' }, { status: 400 });
    }

    const startRes = await fetch(
      'https://generativelanguage.googleapis.com/upload/v1beta/files?key=' + encodeURIComponent(apiKey),
      {
        method: 'POST',
        headers: {
          'X-Goog-Upload-Protocol': 'resumable',
          'X-Goog-Upload-Command': 'start',
          'X-Goog-Upload-Header-Content-Length': String(fileSize),
          'X-Goog-Upload-Header-Content-Type': mimeType || 'application/octet-stream',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ file: { display_name: fileName || 'arquivo' } })
      }
    );

    if (!startRes.ok) {
      let apiMsg = 'Não foi possível iniciar o envio do arquivo.';
      try {
        const errData = await startRes.json();
        if (errData?.error?.message) apiMsg = errData.error.message;
      } catch (e) { /* ignora */ }
      return Response.json({ error: apiMsg }, { status: startRes.status });
    }

    const uploadUrl = startRes.headers.get('x-goog-upload-url');
    if (!uploadUrl) {
      return Response.json({ error: 'O Google não retornou o endereço de envio.' }, { status: 502 });
    }

    return Response.json({ uploadUrl });
  } catch (e) {
    return Response.json({ error: e.message || 'Erro interno ao iniciar o upload.' }, { status: 500 });
  }
};
