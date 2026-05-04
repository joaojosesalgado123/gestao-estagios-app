# Ligix — Connecting Futures

> **Sistema de Gestão de Estágios Académicos**  
> Aplicação Android desenvolvida em Kotlin no âmbito da Unidade Curricular de Computação Móvel  
> Licenciatura em Engenharia Informática — ESTG / Instituto Politécnico de Viana do Castelo  
> Ano letivo 2025/2026

---

## Equipa

| Nome | Número |
|------|--------|
| Duarte Bravo | 31385 |
| Pedro Morais | 31391 |
| João Salgado | 29109 |
| Pedro Vieira | 31389 |

**Docente:** Ricardo Castro

---

## Descrição do Projeto

O **Ligix** é uma aplicação móvel Android para a gestão de estágios académicos. O nome deriva de _"ligação"_ + o sufixo _"ix"_, refletindo o propósito central da plataforma: **conectar alunos, docentes e empresas** de forma eficiente ao longo de todo o ciclo do estágio.

A aplicação suporta o fluxo completo:
- Publicação e pesquisa de ofertas de estágio
- Submissão e gestão de candidaturas
- Acompanhamento de atividades durante o estágio
- Comunicação entre os intervenientes
- Avaliação e classificação final

---

## Funcionalidades Principais

### Perfis de Utilizador
- **Administrador** — gestão de utilizadores, aprovação de empresas e atribuição de orientadores
- **Aluno** — pesquisa de ofertas, candidaturas, registo de atividades e comunicação
- **Docente** — acompanhamento de alunos orientados e avaliação
- **Empresa** — publicação de ofertas, gestão de candidaturas e avaliação

### Módulos
- Autenticação (registo, login, recuperação de password)
- Gestão de Empresas (registo e aprovação)
- Gestão de Ofertas (criação, edição, pesquisa e filtros)
- Candidaturas (submissão com CV e carta de motivação)
- Acompanhamento (registo de atividades, orientadores)
- Comunicação (mensagens entre aluno e orientador)
- Avaliação (empresa + docente, classificação automática)
- Notificações push
- Funcionamento offline com sincronização automática

---

## Requisitos Não Funcionais

- Suporte a **Português e Inglês** (`values/` e `values-en/`)
- Suporte a orientação **portrait e landscape** (`layout/` e `layout-land/`)
- Splash screen no arranque
- Resposta em menos de **2 segundos** em condições normais de rede
- Passwords geridas pelo **Firebase Authentication** (nunca armazenadas em texto simples)
- Controlo de acesso baseado em **perfis e permissões** (Firestore Security Rules por role)

---

## Modelo de Dados

O modelo de dados adota o padrão de **herança de utilizador**, com a entidade base `Utilizador` e entidades derivadas `Aluno`, `Docente`, `Empresa` e `Orientador_Empresa`. A entidade central do sistema é o `Estágio`, que agrega candidatura, orientadores, atividades, relatório final e avaliação.

Principais entidades: `Utilizador`, `Aluno`, `Docente`, `Empresa`, `OfertaEstagio`, `Candidatura`, `Estagio`, `Atividade`, `Avaliacao`, `Item_Avaliacao`, `RelatorioFinal`, `Conversa`, `Mensagem`, `Notificacao`, `Instituicao_Ensino`.

---

## Gestão de Projeto

O projeto foi gerido através do **Trello**:  

---

## Mockups (Figma)

Os mockups de alta fidelidade foram realizados no FIGMA.

---


## Uso de Inteligência Artificial

No âmbito deste projeto, foram utilizadas ferramentas de apoio baseadas em inteligência artificial para auxiliar no apoio à criação de documentação, criação de requisitos, ajuda na modelação de dados - Claude, Chat GPT.
Para a ajuda na criação dos mockups foi usada a plataforma https://stitch.withgoogle.com.

---

## Licença

Projeto académico desenvolvido para a Unidade Curricular de Computação Móvel — ESTG / IPVC.  

