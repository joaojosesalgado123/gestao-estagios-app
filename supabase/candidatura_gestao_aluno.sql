-- Ligix - gestao de candidaturas pelo aluno e garantia de um unico estagio ativo.
-- Executar uma vez no Supabase Dashboard > SQL Editor.

alter table public.candidatura
add column if not exists oculta_aluno boolean not null default false;

do $$
begin
    if exists (
        select 1
        from public.estagio
        group by idcandidatura
        having count(*) > 1
    ) then
        raise exception
            'Existem candidaturas com mais de um estagio. Corrija os duplicados antes de instalar a migracao.';
    end if;

    if exists (
        select 1
        from public.estagio e
        join public.candidatura c on c.idcandidatura = e.idcandidatura
        where e.status = 'ativo'
        group by c.idaluno
        having count(*) > 1
    ) then
        raise exception
            'Existem alunos com mais de um estagio ativo. Corrija os duplicados antes de instalar a migracao.';
    end if;

    if exists (
        select 1
        from public.candidatura
        where status = 'aceite'
        group by idaluno
        having count(*) > 1
    ) then
        raise exception
            'Existem alunos com mais de uma candidatura aceite. Corrija os duplicados antes de instalar a migracao.';
    end if;

    if exists (
        select 1
        from public.candidatura
        where status in ('pendente', 'aceite')
        group by idaluno, idoferta
        having count(*) > 1
    ) then
        raise exception
            'Existem candidaturas ativas duplicadas para a mesma oferta. Corrija os duplicados antes de instalar a migracao.';
    end if;
end;
$$;

create unique index if not exists estagio_idcandidatura_unique_idx
on public.estagio (idcandidatura);

create unique index if not exists candidatura_um_aceite_por_aluno_idx
on public.candidatura (idaluno)
where status = 'aceite';

create unique index if not exists candidatura_ativa_aluno_oferta_unique_idx
on public.candidatura (idaluno, idoferta)
where status in ('pendente', 'aceite');

-- O aluno pode desistir apenas das suas candidaturas que ainda estao pendentes.
create or replace function public.cancelar_candidatura_aluno(p_idcandidatura uuid)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is null then
        return false;
    end if;

    update public.candidatura
    set status = 'cancelada',
        oculta_aluno = true
    where idcandidatura = p_idcandidatura
      and idaluno = auth.uid()
      and status = 'pendente';

    return found;
end;
$$;

revoke all on function public.cancelar_candidatura_aluno(uuid) from public;
grant execute on function public.cancelar_candidatura_aluno(uuid) to authenticated;

-- Uma rejeicao permanece no historico, mas o aluno pode remove-la da sua lista.
create or replace function public.ocultar_resultado_candidatura_aluno(p_idcandidatura uuid)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
begin
    if auth.uid() is null then
        return false;
    end if;

    update public.candidatura
    set oculta_aluno = true
    where idcandidatura = p_idcandidatura
      and idaluno = auth.uid()
      and status = 'rejeitada';

    return found;
end;
$$;

revoke all on function public.ocultar_resultado_candidatura_aluno(uuid) from public;
grant execute on function public.ocultar_resultado_candidatura_aluno(uuid) to authenticated;

-- Substitui a versao anterior: cada aluno pode ter apenas um estagio ativo.
-- Ao aceitar uma candidatura, as restantes candidaturas pendentes sao canceladas.
create or replace function public.criar_estagio_ao_aceitar_candidatura()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.status = 'aceite' then
        if exists (
            select 1
            from public.estagio e
            join public.candidatura c on c.idcandidatura = e.idcandidatura
            where c.idaluno = new.idaluno
              and c.idcandidatura <> new.idcandidatura
              and e.status = 'ativo'
        ) then
            raise exception
                'O aluno ja possui um estagio ativo.';
        end if;

        insert into public.estagio (
            idestagio,
            status,
            idcandidatura
        )
        values (
            gen_random_uuid(),
            'ativo',
            new.idcandidatura
        )
        on conflict (idcandidatura) do nothing;

        update public.candidatura
        set status = 'cancelada',
            oculta_aluno = true
        where idaluno = new.idaluno
          and idcandidatura <> new.idcandidatura
          and status = 'pendente';
    end if;

    return new;
end;
$$;

drop trigger if exists candidatura_aceite_cria_estagio on public.candidatura;

create trigger candidatura_aceite_cria_estagio
after insert or update of status on public.candidatura
for each row
execute function public.criar_estagio_ao_aceitar_candidatura();

-- Preenche candidaturas aceites antes da instalacao do trigger.
insert into public.estagio (
    idestagio,
    status,
    idcandidatura
)
select
    gen_random_uuid(),
    'ativo',
    c.idcandidatura
from public.candidatura c
where c.status = 'aceite'
on conflict (idcandidatura) do nothing;

-- Oculta candidaturas pendentes antigas de alunos que ja possuem estagio ativo.
update public.candidatura c
set status = 'cancelada',
    oculta_aluno = true
where c.status = 'pendente'
  and exists (
      select 1
      from public.estagio e
      join public.candidatura aceite on aceite.idcandidatura = e.idcandidatura
      where aceite.idaluno = c.idaluno
        and e.status = 'ativo'
  );
