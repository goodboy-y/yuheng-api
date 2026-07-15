import { defineConfig } from 'vitepress'

export default defineConfig({
  title: '玉衡API',
  description: '将SQL直接转换为API接口的低代码平台',
  lang: 'zh-CN',
  base: '/yuheng-api/',
  lastUpdated: true,

  head: [
    ['link', { rel: 'icon', href: '/yuheng-api/favicon.ico' }],
  ],

  themeConfig: {
    logo: '/logo.svg',
    nav: [
      { text: '指南', link: '/guide/getting-started' },
      { text: '功能', link: '/features/datasource' },
      { text: '架构', link: '/architecture/overview' },
      { text: 'API', link: '/api/admin-api' },
      { text: 'SDK', link: '/sdk/java-sdk' },
    ],

    sidebar: {
      '/guide/': [
        {
          text: '指南',
          items: [
            { text: '快速开始', link: '/guide/getting-started' },
            { text: '安装部署', link: '/guide/installation' },
            { text: '快速示例', link: '/guide/quick-example' },
          ],
        },
      ],
      '/features/': [
        {
          text: '功能',
          items: [
            { text: '概览', link: '/features/' },
            { text: '数据源管理', link: '/features/datasource' },
            { text: 'API配置', link: '/features/api-config' },
            { text: '客户端管理', link: '/features/client' },
            { text: '插件系统', link: '/features/plugin' },
            { text: '字段映射', link: '/features/field-mapping' },
            { text: '访问控制', link: '/features/access-control' },
            { text: '数据导出', link: '/features/export' },
          ],
        },
      ],
      '/architecture/': [
        {
          text: '架构',
          items: [
            { text: '架构概览', link: '/architecture/overview' },
            { text: '数据库设计', link: '/architecture/database' },
            { text: '安全设计', link: '/architecture/security' },
          ],
        },
      ],
      '/api/': [
        {
          text: 'API接口',
          items: [
            { text: '管理端API', link: '/api/admin-api' },
            { text: '数据API', link: '/api/data-api' },
          ],
        },
      ],
      '/sdk/': [
        {
          text: 'SDK',
          items: [
            { text: 'Java SDK', link: '/sdk/java-sdk' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/goodboy-y/yuheng-api' },
    ],

    search: {
      provider: 'local',
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: {
        dateStyle: 'short',
        timeStyle: 'short',
      },
    },

    footer: {
      message: '基于 MIT License 发布',
      copyright: 'Copyright © 2024 玉衡API',
    },
  },
})
