-- Ligix - suporte incremental para avaliacao final do estagio.
-- Executar no Supabase Dashboard > SQL Editor se a avaliacao final falhar por RLS.

alter table public.avaliacao enable row level security;
alter table public.item_avaliacao enable row level security;

grant select, insert on public.avaliacao to authenticated;
grant select, insert on public.item_avaliacao to authenticated;

drop policy if exists "avaliacao_select_participantes" on public.avaliacao;
create policy "avaliacao_select_participantes"
on public.avaliacao for select
to authenticated
using (public.can_access_estagio(idestagio));

drop policy if exists "avaliacao_insert_participantes" on public.avaliacao;
create policy "avaliacao_insert_participantes"
on public.avaliacao for insert
to authenticated
with check (
    exists (
        select 1
        from public.estagio e
        where e.idestagio = avaliacao.idestagio
          and (
              e.iddocente = auth.uid()
              or e.idorientador = auth.uid()
              or public.is_admin()
          )
    )
);

drop policy if exists "item_avaliacao_select_participantes" on public.item_avaliacao;
create policy "item_avaliacao_select_participantes"
on public.item_avaliacao for select
to authenticated
using (
    exists (
        select 1
        from public.avaliacao a
        where a.idavaliacao = item_avaliacao.idavaliacao
          and public.can_access_estagio(a.idestagio)
    )
);

drop policy if exists "item_avaliacao_insert_self" on public.item_avaliacao;
create policy "item_avaliacao_insert_self"
on public.item_avaliacao for insert
to authenticated
with check (
    idavaliador = auth.uid()
    and exists (
        select 1
        from public.avaliacao a
        join public.estagio e on e.idestagio = a.idestagio
        where a.idavaliacao = item_avaliacao.idavaliacao
          and (
              e.iddocente = auth.uid()
              or e.idorientador = auth.uid()
              or public.is_admin()
          )
    )
);
