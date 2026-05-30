-- Ligix - suporte à sincronização offline de atividades.
-- Executar uma vez no Supabase Dashboard > SQL Editor.

-- O cliente reutiliza o mesmo UUID em cada tentativa. O índice permite que o
-- POST com Prefer: resolution=merge-duplicates seja idempotente.
create unique index if not exists atividade_idatividade_unique_idx
on public.atividade (idatividade);

drop policy if exists "atividade_delete_participantes" on public.atividade;
create policy "atividade_delete_participantes"
on public.atividade for delete
to authenticated
using (public.can_access_estagio(idestagio));
