-- Ligix - suporte incremental para perfis de docente e orientador.
-- Executar depois do setup antigo, se ainda nao tiveres estas policies/trigger atualizados.

-- O registo de docente envia estes metadados pelo Supabase Auth:
-- telemovel, area e idinstituicao.
create or replace function public.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    metadata jsonb := coalesce(new.raw_user_meta_data, '{}'::jsonb);
    profile_role text := coalesce(metadata->>'role', 'aluno');
begin
    insert into public.utilizador (
        idutilizador,
        username,
        nome,
        email,
        role
    )
    values (
        new.id,
        coalesce(nullif(metadata->>'username', ''), split_part(new.email, '@', 1)),
        coalesce(metadata->>'nome', ''),
        new.email,
        profile_role
    )
    on conflict (idutilizador) do update
    set username = excluded.username,
        nome = excluded.nome,
        email = excluded.email,
        role = excluded.role;

    if profile_role = 'aluno' then
        insert into public.aluno (
            idutilizador,
            numero_aluno,
            curso,
            telemovel,
            idinstituicao
        )
        values (
            new.id,
            coalesce(metadata->>'numero_aluno', ''),
            coalesce(metadata->>'curso', ''),
            nullif(metadata->>'telemovel', ''),
            nullif(metadata->>'idinstituicao', '')::uuid
        )
        on conflict (idutilizador) do update
        set numero_aluno = excluded.numero_aluno,
            curso = excluded.curso,
            telemovel = excluded.telemovel,
            idinstituicao = excluded.idinstituicao;
    elsif profile_role = 'docente' then
        insert into public.docente (
            idutilizador,
            telemovel,
            area,
            idinstituicao
        )
        values (
            new.id,
            nullif(metadata->>'telemovel', ''),
            nullif(metadata->>'area', ''),
            nullif(metadata->>'idinstituicao', '')::uuid
        )
        on conflict (idutilizador) do update
        set telemovel = excluded.telemovel,
            area = excluded.area,
            idinstituicao = excluded.idinstituicao;
    elsif profile_role = 'empresa' then
        insert into public.empresa (
            idutilizador,
            nipc,
            morada,
            telemovel,
            descricao,
            status
        )
        values (
            new.id,
            nullif(metadata->>'nipc', ''),
            nullif(metadata->>'morada', ''),
            nullif(metadata->>'telemovel', ''),
            nullif(metadata->>'descricao', ''),
            coalesce(nullif(metadata->>'status', ''), 'pendente')
        )
        on conflict (idutilizador) do update
        set nipc = excluded.nipc,
            morada = excluded.morada,
            telemovel = excluded.telemovel,
            descricao = excluded.descricao,
            status = excluded.status;
    end if;

    return new;
end;
$$;

-- A app lista instituicoes no registo de docente, antes de haver login.
alter table public.instituicao_ensino enable row level security;
grant select on public.instituicao_ensino to anon, authenticated;

drop policy if exists "instituicao_select_all" on public.instituicao_ensino;
create policy "instituicao_select_all"
on public.instituicao_ensino for select
to anon, authenticated
using (true);

-- Perfil do docente: editar telemovel, area e idinstituicao.
drop policy if exists "docente_update_own" on public.docente;
create policy "docente_update_own"
on public.docente for update
to authenticated
using (idutilizador = auth.uid())
with check (idutilizador = auth.uid());

-- Criacao/edicao de orientadores pela empresa e edicao do proprio perfil pelo orientador.
alter table public.orientador_empresa enable row level security;
grant select, insert, update, delete on public.orientador_empresa to authenticated;

drop policy if exists "orientador_empresa_select_own_company_admin" on public.orientador_empresa;
create policy "orientador_empresa_select_own_company_admin"
on public.orientador_empresa for select
to authenticated
using (
    idutilizador = auth.uid()
    or idempresa = auth.uid()
    or public.is_admin()
);

drop policy if exists "empresa_insert_own_orientador" on public.orientador_empresa;
create policy "empresa_insert_own_orientador"
on public.orientador_empresa for insert
to authenticated
with check (
    idempresa = auth.uid()
    or public.is_admin()
);

drop policy if exists "orientador_empresa_update_own_or_company" on public.orientador_empresa;
create policy "orientador_empresa_update_own_or_company"
on public.orientador_empresa for update
to authenticated
using (
    idutilizador = auth.uid()
    or idempresa = auth.uid()
    or public.is_admin()
)
with check (
    idutilizador = auth.uid()
    or idempresa = auth.uid()
    or public.is_admin()
);

drop policy if exists "empresa_delete_own_orientador" on public.orientador_empresa;
create policy "empresa_delete_own_orientador"
on public.orientador_empresa for delete
to authenticated
using (
    idempresa = auth.uid()
    or public.is_admin()
);
