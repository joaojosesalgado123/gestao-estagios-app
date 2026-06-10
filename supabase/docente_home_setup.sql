-- Ligix - suporte para o painel do docente.
-- Executar no Supabase Dashboard > SQL Editor antes de testar o painel do docente.

drop policy if exists "docente_update_own" on public.docente;
create policy "docente_update_own"
on public.docente for update
to authenticated
using (idutilizador = auth.uid())
with check (idutilizador = auth.uid());

-- O docente precisa de ver dados dos alunos/empresas ligados aos seus estágios.
drop policy if exists "utilizador_select_estagio_participantes" on public.utilizador;
create policy "utilizador_select_estagio_participantes"
on public.utilizador for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        join public.oferta_estagio o on o.idoferta = c.idoferta
        where public.can_access_estagio(e.idestagio)
          and (
              utilizador.idutilizador = c.idaluno
              or utilizador.idutilizador = e.iddocente
              or utilizador.idutilizador = e.idorientador
              or utilizador.idutilizador = o.idempresa
          )
    )
);

drop policy if exists "aluno_select_estagio_participantes" on public.aluno;
create policy "aluno_select_estagio_participantes"
on public.aluno for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        where c.idaluno = aluno.idutilizador
          and public.can_access_estagio(e.idestagio)
    )
);

drop policy if exists "candidatura_select_estagio_participantes" on public.candidatura;
create policy "candidatura_select_estagio_participantes"
on public.candidatura for select
to authenticated
using (
    public.is_admin()
    or exists (
        select 1
        from public.estagio e
        where e.idcandidatura = candidatura.idcandidatura
          and public.can_access_estagio(e.idestagio)
    )
);
