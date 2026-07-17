// Consulta o status de processamento de um arquivo já enviado (PROCESSING / ACTIVE / FAILED).
// É POST (em vez de GET) de propósito: assim o service worker do app nunca
// guarda essa resposta em cache, o que quebraria o polling de status.
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
    const { name } = await req.json();
    if (!name) {
      return Response.json({ error: 'Parâmetro "name" é obrigatório.' }, { status: 400 });
    }

    const res = await fetch(
      'https://generativelanguage.googleapis.com/v1beta/' + name + '?key=' + encodeURIComponent(apiKey)
    );
    const data = await res.json();

    if (!res.ok) {
      return Response.json({ error: data?.error?.message || 'Falha ao consultar o arquivo.' }, { status: res.status });
    }

    return Response.json(data);
  } catch (e) {
    return Response.json({ error: e.message || 'Erro interno ao consultar o arquivo.' }, { status: 500 });
  }
};
